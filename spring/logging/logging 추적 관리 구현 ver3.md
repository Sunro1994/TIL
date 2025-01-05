# Logging Trace V3

V2에서는 TraceId의 동기화 문제는 해결했지만 모든 파라미터에 대한 수정의 필요성이 생겨 유지 보수에 어려움이 발생한다는 문제와 beginSync메서드를 반드시 수행해야 한다는 문제가 있다.

이러한 문제를 해결하기 위한 V3를 구현해보려 한다.
첫 번째 방법은 LogTrace라는 추상체로 중복되는 작업에 대한 메서드를 공통 모듈로 만들고 구현체를 사용하는 방법이다.

## 목차
- [Logging Trace V3](#logging-trace-v3)
    - [TraceId 변경점](#traceid-변경점)
        - [createId](#createid)
        - [isFirstLevel](#isfirstlevel)
        - [createPreviousId](#createpreviousid)
    - [LogTrace](#logtrace)
    - [FieldLogTrace](#fieldlogtrace)
        - [추가 필드 traceHolder](#추가-필드-traceholder)
        - [begin 메서드](#begin-메서드)
        - [end, Exception의 complete() 메서드](#endexception의-complete-메서드)
    - [결과](#결과)
    - [문제점](#문제점)

## TraceId 변경점

### createId
level의 값을 1증가시킨 새로운 TraceId 객체를 생성하지만 매개변수로 traceId를 받으므로 기존의 객체에서 값만 변경시킨것과 동일하다.

### isFirstLevel
노드의 첫 단계인지 확인한다.

### createPreviousId()
FieldLogTrace의 complete메서드에서 수행되는 메서드이다. 순회가 끝나고 값을 반환하는 과정에서 노드를 감소시키며 상위 단계로 돌아온다.

```java
public TraceId createNextId() {
        return new TraceId(traceId, level + 1);
    }

    public boolean isFirstLevel() {
        return level == 0;
    }

    public TraceId createPreviousId() {
        return new TraceId(traceId, level - 1);
    }
```
## LogTrace

모든 로직에서 공통으로 수행하는 세가지 메서드를 추상화한다.
```java
public interface LogTrace
{
    TraceStatus begin(String msg);
    void end(TraceStatus status);
    void exception(TraceStatus status, Exception e);

}
```

## FieldLogTrace
기존의 TraceV1, V2 코드와 다른 몇 가지가 있다. 아래에서 추가된 필드와 메서드를 하나씩 설명하겠다.
```java


@Component
@Slf4j
public class FieldLogTrace implements LogTrace{
    private static final String START_PREFIX = "-->";
    private static final String END_PREFIX = "<--";
    private static final String EXCEPTION_PREFIX = "<X-";

    private TraceId traceIdHolder;

    @Override
    public TraceStatus begin(String msg) {
        syncTraceId();
        TraceId traceId = traceIdHolder;
        Long startTimeMills = currentTimeMillis();
        log.info("[{}] {}{}", traceId.getTraceId(), addSpace(START_PREFIX, traceId.getLevel()), msg);
        return new TraceStatus(traceId, startTimeMills, msg);
    }

    private static String addSpace(String prefix, int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append((i==level-1)? "|"+prefix : "| ");
        }
        return sb.toString();
    }

    private void syncTraceId() {
        if (traceIdHolder == null) {
            traceIdHolder = new TraceId();
        } else {
            traceIdHolder = traceIdHolder.createNextId();
        }
    }

    @Override
    public void end(TraceStatus status) {
        complete(status, null);

    }

    @Override
    public void exception(TraceStatus status, Exception e) {
        complete(status, e);

    }

    private void complete(TraceStatus status, Exception e) {
        Long stopTimeMs = currentTimeMillis();
        long resultTimeMs = stopTimeMs - status.getStartTime();
        TraceId traceId = status.getTraceId();

        if (e == null) {
            log.info("[{}] {}{} time={}ms", traceId.getTraceId(),
                    addSpace(END_PREFIX, traceId.getLevel()), status.getMessage(),
                    resultTimeMs);
        } else {
            log.info("[{}] {}{} time={}ms ex={}", traceId.getTraceId(),
                    addSpace(EXCEPTION_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs,
                    e.toString());
        }

        releaseTraceId();
    }

    private void releaseTraceId() {
        if (traceIdHolder.isFirstLevel()) {
            traceIdHolder = null; // 1 -> destroy
        } else {
            traceIdHolder = traceIdHolder.createPreviousId(); // 1 -> 2 -> 3(complete) -> 2 -> 1
        }
    }
}
```


### 추가 필드 traceHolder

TraceId객체를 필드로 분리하여 메서드에서 호출하는 방식에서 변경되었다.

### begin 메서드
syncTraceId() 메서드를 사용하여 시작과정에서 traceId의 동기화를 진행한다.
처음 호출되었다면 새로 생성된 level 0번의 아이디를 새로 부여하고 그게 아니라면 기존의 id값의 레벨을 증가시킨다.
```java
@Override
    public TraceStatus begin(String msg) {
        syncTraceId();
        TraceId traceId = traceIdHolder;
        Long startTimeMills = currentTimeMillis();
        log.info("[{}] {}{}", traceId.getTraceId(), addSpace(START_PREFIX, traceId.getLevel()), msg);
        return new TraceStatus(traceId, startTimeMills, msg);
    }
private void syncTraceId() {
    if (traceIdHolder == null) {
        traceIdHolder = new TraceId();
    } else {
        traceIdHolder = traceIdHolder.createNextId();
    }
}
```

### end,Exception의 complete() 메서드

기존의 코드와 동일하지만 releaseTraceId 메서드가 추가되었다. 수행을 마친 작업에 대해 level을 감소시키며 첫 번째 노드로 돌아왔다면 traceIdHolder를 힙영역에서 소멸시킨다.

```java
private void complete(TraceStatus status, Exception e) {
        Long stopTimeMs = currentTimeMillis();
        long resultTimeMs = stopTimeMs - status.getStartTime();
        TraceId traceId = status.getTraceId();

        if (e == null) {
            log.info("[{}] {}{} time={}ms", traceId.getTraceId(),
                    addSpace(END_PREFIX, traceId.getLevel()), status.getMessage(),
                    resultTimeMs);
        } else {
            log.info("[{}] {}{} time={}ms ex={}", traceId.getTraceId(),
                    addSpace(EXCEPTION_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs,
                    e.toString());
        }

        releaseTraceId();
    }

    private void releaseTraceId() {
        if (traceIdHolder.isFirstLevel()) {
            traceIdHolder = null; // 1 -> destroy
        } else {
            traceIdHolder = traceIdHolder.createPreviousId(); // 1 -> 2 -> 3(complete) -> 2 -> 1
        }
    }
```

### 결과
기존의 V2를 추상체인 LogTace 객체로 변경 후 수행했더니 정상적으로 수행됐다.
```shell
2024-12-26T02:33:44.415+09:00  WARN 5391 --- [l-1 housekeeper] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Retrograde clock change detected (housekeeper delta=29s565ms), soft-evicting connections from pool.
2024-12-26T02:48:16.604+09:00  INFO 5391 --- [io-8080-exec-10] c.e.l.global.trace.FieldLogTrace         : [75c0f349] UserController.create()
2024-12-26T02:48:16.605+09:00  INFO 5391 --- [io-8080-exec-10] c.e.l.global.trace.FieldLogTrace         : [75c0f349] |-->UserService.create()
2024-12-26T02:48:16.620+09:00  INFO 5391 --- [io-8080-exec-10] c.e.l.global.trace.FieldLogTrace         : [75c0f349] |<--UserService.create() time=15ms
2024-12-26T02:48:16.620+09:00  INFO 5391 --- [io-8080-exec-10] c.e.l.global.trace.FieldLogTrace         : [75c0f349] UserController.create() time=16ms
```

정말 잘된것일까? 테스트를 하기 위해 Jmeter로 동시에 5명이 해당 API를 호출해보는것을 가정해보았다.

## 문제점

보다시피 5개의 쓰레드가 동작하고 각각의 고유 TraceID값을 보유하고 있다. 하지만 스프링부트에서는 싱글톤으로 객체를 생성하고 관리하기 때문에
traceHolder를 동시에 사용하다보니 값이 순서대로 작동하는게 아닌 어떤 쓰레드가 사용중인 값을 다른 쓰레드가 사용하다보니 얽히고 섥혀서 꼬이게 된것이다.
다시 말해 워낼 isFirstLevel이 true여야 하는 값이 이미 2,3과 같은 값을 갖고 있는 것이다.
다음에 해결해야 하는 문제는 동시성의 해결이였다. 이 과정은 V4로 작성하려고 한다.

```shell
: [75c0f349] UserController.create()
2024-12-26T02:48:16.605+09:00  INFO 5391 --- [io-8080-exec-10] c.e.l.global.trace.FieldLogTrace         : [75c0f349] |-->UserService.create()
2024-12-26T02:48:16.620+09:00  INFO 5391 --- [io-8080-exec-10] c.e.l.global.trace.FieldLogTrace         : [75c0f349] |<--UserService.create() time=15ms
2024-12-26T02:48:16.620+09:00  INFO 5391 --- [io-8080-exec-10] c.e.l.global.trace.FieldLogTrace         : [75c0f349] UserController.create() time=16ms
2024-12-26T02:51:15.256+09:00  INFO 5391 --- [nio-8080-exec-1] c.e.l.global.trace.FieldLogTrace         : [b6cefb85] UserController.create()
2024-12-26T02:51:15.256+09:00  INFO 5391 --- [nio-8080-exec-5] c.e.l.global.trace.FieldLogTrace         : [4aaa902f] UserController.create()
2024-12-26T02:51:15.256+09:00  INFO 5391 --- [nio-8080-exec-2] c.e.l.global.trace.FieldLogTrace         : [6e8209cb] UserController.create()
2024-12-26T02:51:15.256+09:00  INFO 5391 --- [nio-8080-exec-3] c.e.l.global.trace.FieldLogTrace         : [4c0eb851] UserController.create()
2024-12-26T02:51:15.257+09:00  INFO 5391 --- [nio-8080-exec-1] c.e.l.global.trace.FieldLogTrace         : [4c0eb851] |-->UserService.create()
2024-12-26T02:51:15.257+09:00  INFO 5391 --- [nio-8080-exec-5] c.e.l.global.trace.FieldLogTrace         : [4c0eb851] | |-->UserService.create()
2024-12-26T02:51:15.257+09:00  INFO 5391 --- [nio-8080-exec-2] c.e.l.global.trace.FieldLogTrace         : [4c0eb851] | | | |-->UserService.create()
2024-12-26T02:51:15.256+09:00  INFO 5391 --- [nio-8080-exec-6] c.e.l.global.trace.FieldLogTrace         : [ad99b3a2] UserController.create()
2024-12-26T02:51:15.257+09:00  INFO 5391 --- [nio-8080-exec-3] c.e.l.global.trace.FieldLogTrace         : [4c0eb851] | | |-->UserService.create()
2024-12-26T02:51:15.257+09:00  INFO 5391 --- [nio-8080-exec-6] c.e.l.global.trace.FieldLogTrace         : [4c0eb851] | | | | |-->UserService.create()
2024-12-26T02:51:15.275+09:00  INFO 5391 --- [nio-8080-exec-6] c.e.l.global.trace.FieldLogTrace         : [4c0eb851] | | | | |<--UserService.create() time=18ms
2024-12-26T02:51:15.275+09:00  INFO 5391 --- [nio-8080-exec-6] c.e.l.global.trace.FieldLogTrace         : [ad99b3a2] UserController.create() time=19ms
2024-12-26T02:51:15.275+09:00  INFO 5391 --- [nio-8080-exec-3] c.e.l.global.trace.FieldLogTrace         : [4c0eb851] | | |<--UserService.create() time=18ms
2024-12-26T02:51:15.275+09:00  INFO 5391 --- [nio-8080-exec-5] c.e.l.global.trace.FieldLogTrace         : [4c0eb851] | |<--UserService.create() time=18ms
2024-12-26T02:51:15.275+09:00  INFO 5391 --- [nio-8080-exec-3] c.e.l.global.trace.FieldLogTrace         : [4c0eb851] UserController.create() time=19ms
2024-12-26T02:51:15.275+09:00  INFO 5391 --- [nio-8080-exec-5] c.e.l.global.trace.FieldLogTrace         : [4aaa902f] UserController.create() time=19ms
2024-12-26T02:51:15.275+09:00  INFO 5391 --- [nio-8080-exec-1] c.e.l.global.trace.FieldLogTrace         : [4c0eb851] |<--UserService.create() time=18ms
2024-12-26T02:51:15.275+09:00  INFO 5391 --- [nio-8080-exec-2] c.e.l.global.trace.FieldLogTrace         : [4c0eb851] | | | |<--UserService.create() time=18ms
2024-12-26T02:51:15.275+09:00  INFO 5391 --- [nio-8080-exec-2] c.e.l.global.trace.FieldLogTrace         : [6e8209cb] UserController.create() time=19ms ex=java.lang.NullPointerException: Cannot invoke "com.example.loggingtest.global.trace.TraceId.isFirstLevel()" because "this.traceIdHolder" is null
2024-12-26T02:51:15.275+09:00  INFO 5391 --- [nio-8080-exec-1] c.e.l.global.trace.FieldLogTrace         : [b6cefb85] UserController.create() time=19ms ex=java.lang.NullPointerException: Cannot invoke "com.example.loggingtest.global.trace.TraceId.isFirstLevel()" because "this.traceIdHolder" is null
2024-12-26T02:51:15.277+09:00 ERROR 5391 --- [nio-8080-exec-2] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: java.lang.NullPointerException: Cannot invoke "com.example.loggingtest.global.trace.TraceId.isFirstLevel()" because "this.traceIdHolder" is null] with root cause

java.lang.NullPointerException: Cannot invoke "com.example.loggingtest.global.trace.TraceId.isFirstLevel()" because "this.traceIdHolder" is null
```


