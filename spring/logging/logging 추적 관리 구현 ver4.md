# Logging Trace V4

V3에서 진행했던 방식의 문제점이였던 동시성을 `ThreadLocal`을 사용해서 해결해보려 한다.
ThreadLocal은 다른 쓰레드와 자원을 공유하지 않는 독립적인 영역이다.
그러므로 동시에 수행해도 각자 같은 이름의 변수이지만 다른 객체를 사용하고 있는 것이다.

큰 변동사항없이 기존에 사용했던 TraceIdHolder의 타입을 ThreadLocal<TraceId>로 바꿔주고 문법에 맞게 변경해준다.

## FieldLogTrace

traceIdHolder, syncTraceId, begin, releaseTraceId에서 ThreadLocal의 get(),set()메서드를 사용하도록 수정해주면 된다.

```java
package com.example.loggingtest.global.trace;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static java.lang.System.currentTimeMillis;

@Component
@Slf4j
public class FieldLogTrace implements LogTrace {
    private static final String START_PREFIX = "-->";
    private static final String END_PREFIX = "<--";
    private static final String EXCEPTION_PREFIX = "<X-";

    private ThreadLocal<TraceId> traceIdHolder = new ThreadLocal<>();

    @Override
    public TraceStatus begin(String msg) {
        syncTraceId();
        TraceId traceId = traceIdHolder.get();
        Long startTimeMills = currentTimeMillis();
        log.info("[{}] {}{}", traceId.getTraceId(), addSpace(START_PREFIX, traceId.getLevel()), msg);
        return new TraceStatus(traceId, startTimeMills, msg);
    }

    private static String addSpace(String prefix, int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append((i == level - 1) ? "|" + prefix : "| ");
        }
        return sb.toString();
    }

    private void syncTraceId() {
        TraceId traceId = traceIdHolder.get();
        if (traceId == null) {
            traceIdHolder.set(new TraceId());
        } else {
            traceIdHolder.set(traceId.createNextId());
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
        TraceId traceId = traceIdHolder.get();
        if (traceId.isFirstLevel()) {
            traceIdHolder.remove();
        } else {
            traceIdHolder.set(traceId.createPreviousId()); // 1 -> 2 -> 3(complete) -> 2 -> 1
        }
    }
}

```


## 결과
각각의 쓰레드가 올바르게 작동하고 종료된다.

```shell
[2e492cdd] UserController.create()
2024-12-26T03:25:24.385+09:00  INFO 5900 --- [nio-8080-exec-2] c.e.l.global.trace.FieldLogTrace         : [469fd187] UserController.create()
2024-12-26T03:25:24.385+09:00  INFO 5900 --- [nio-8080-exec-4] c.e.l.global.trace.FieldLogTrace         : [f4b3b998] UserController.create()
2024-12-26T03:25:24.385+09:00  INFO 5900 --- [nio-8080-exec-1] c.e.l.global.trace.FieldLogTrace         : [a9f4a294] UserController.create()
2024-12-26T03:25:24.385+09:00  INFO 5900 --- [nio-8080-exec-5] c.e.l.global.trace.FieldLogTrace         : [fb789422] UserController.create()
2024-12-26T03:25:24.386+09:00  INFO 5900 --- [nio-8080-exec-5] c.e.l.global.trace.FieldLogTrace         : [fb789422] |-->UserService.create()
2024-12-26T03:25:24.386+09:00  INFO 5900 --- [nio-8080-exec-3] c.e.l.global.trace.FieldLogTrace         : [2e492cdd] |-->UserService.create()
2024-12-26T03:25:24.386+09:00  INFO 5900 --- [nio-8080-exec-4] c.e.l.global.trace.FieldLogTrace         : [f4b3b998] |-->UserService.create()
2024-12-26T03:25:24.386+09:00  INFO 5900 --- [nio-8080-exec-2] c.e.l.global.trace.FieldLogTrace         : [469fd187] |-->UserService.create()
2024-12-26T03:25:24.386+09:00  INFO 5900 --- [nio-8080-exec-1] c.e.l.global.trace.FieldLogTrace         : [a9f4a294] |-->UserService.create()
2024-12-26T03:25:24.421+09:00  INFO 5900 --- [nio-8080-exec-4] c.e.l.global.trace.FieldLogTrace         : [f4b3b998] |<--UserService.create() time=36ms
2024-12-26T03:25:24.421+09:00  INFO 5900 --- [nio-8080-exec-4] c.e.l.global.trace.FieldLogTrace         : [f4b3b998] UserController.create() time=36ms
2024-12-26T03:25:24.421+09:00  INFO 5900 --- [nio-8080-exec-1] c.e.l.global.trace.FieldLogTrace         : [a9f4a294] |<--UserService.create() time=36ms
2024-12-26T03:25:24.421+09:00  INFO 5900 --- [nio-8080-exec-5] c.e.l.global.trace.FieldLogTrace         : [fb789422] |<--UserService.create() time=36ms
2024-12-26T03:25:24.421+09:00  INFO 5900 --- [nio-8080-exec-1] c.e.l.global.trace.FieldLogTrace         : [a9f4a294] UserController.create() time=36ms
2024-12-26T03:25:24.421+09:00  INFO 5900 --- [nio-8080-exec-2] c.e.l.global.trace.FieldLogTrace         : [469fd187] |<--UserService.create() time=36ms
2024-12-26T03:25:24.421+09:00  INFO 5900 --- [nio-8080-exec-2] c.e.l.global.trace.FieldLogTrace         : [469fd187] UserController.create() time=36ms
2024-12-26T03:25:24.421+09:00  INFO 5900 --- [nio-8080-exec-5] c.e.l.global.trace.FieldLogTrace         : [fb789422] UserController.create() time=36ms
2024-12-26T03:25:24.421+09:00  INFO 5900 --- [nio-8080-exec-3] c.e.l.global.trace.FieldLogTrace         : [2e492cdd] |<--UserService.create() time=36ms
2024-12-26T03:25:24.421+09:00  INFO 5900 --- [nio-8080-exec-3] c.e.l.global.trace.FieldLogTrace         : [2e492cdd] UserController.create() time=36ms

```

## 문제점

이제 기능적으로는 잘 작동하고 발생하는 문제는 없어보인다. 하지만 구조적인 문제가 남아있다.
구조적인 문제점을 해결하기 위해 디자인 패턴에 대한 고민을 다음장에서 해보려고 한다.
