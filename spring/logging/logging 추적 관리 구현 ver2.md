# Logging Trace V2
[1편](logging%20추적%20관리%20구현%20ver1.md)에서 V1의 문제점을 알아보았기 때문에 2편에서는 설명없이 바로 TraceId값의 동기화 문제점을 해결할 것이다.

## 목차
- [TraceV2](#tracev2)
- [TraceId에서 추가된 메서드](#traceid에서-추가된-메서드)
- [Test](#test)
    - [Controller](#controller)
    - [Service](#service)
- [결과](#결과)
- [문제점](#문제점)

---

## TraceV2
V2에서는 beginSync()메서드를 호출하여 traceId값을 일관적으로 사용할 수 있도록 동기화하고 depth를 설정해준다.
```java

@Slf4j
@Component
public class TraceV2 {

    private static  final String START_PREFIX= "-->";
    private static final String END_PREFIX = "<--";
    private static final String EXCEPTION_PREFIX = "<X-";

    public TraceStatus begin(String message) {
        TraceId traceId = new TraceId();
        Long startTime = System.currentTimeMillis();

        log.info("[{}] {}{}",traceId,addSpace(START_PREFIX,traceId.getLevel()),message);
        return new TraceStatus(traceId,startTime,message);
    }
    public TraceStatus beginSync(TraceId beforeTraceId, String message) {
        TraceId traceId = beforeTraceId.createNextId();
        long startTime = System.currentTimeMillis();
        log.info("[{}] {}{}",traceId,addSpace(START_PREFIX,traceId.getLevel()),message);

        return new TraceStatus(traceId,startTime,message);
    }

    public void end(TraceStatus traceStatus) {
        complete(traceStatus,null);
    }

    public void exception(TraceStatus status, Exception e) {
        complete(status, e);
    }

    private void complete(TraceStatus traceStatus, Exception e) {
        Long stopMills = System.currentTimeMillis();
        long resultMillls = stopMills - traceStatus.getStartTime();
        TraceId traceId = traceStatus.getTraceId();

        if(e==null){
            log.info("[{}] {}{} time={}ms", traceId.getTraceId(), addSpace(END_PREFIX, traceId.getLevel()),traceStatus.getMessage(),resultMillls );
        }else{
            log.info("[{}] {}{} time={}ms, ex={}",traceId.getTraceId(), addSpace(EXCEPTION_PREFIX, traceId.getLevel()),traceStatus.getMessage(),resultMillls,e.toString());
        }
    }

    private static String addSpace(String prefix, int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append((i==level-1)? "|"+prefix : "|    ");
        }
        return sb.toString();
    }
}

```
## TraceId에서 추가된 메서드
```java
public TraceId createNextId() {
        return new TraceId(traceId, level + 1);
    }
```

## Test
이전과 다른 점은 각 레이어에서 생성된 traceId값을 다음 레이어로 전달해준다는 점이다.

### Controller
```java
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;
    private final TraceV2 trace;

    @GetMapping
    public String create(@RequestParam String name) {
        TraceStatus status =trace.begin("UserController.create()");
        String answer = null;
        try{
             answer = userService.create(status.getTraceId(),name);
            trace.end(status);
        }catch (Exception e){
            trace.exception(status, e);
            throw e;
        }
        return answer;
    }

}

```

### Service
보시다시피 TraceV2에서는 Controller에서 넘겨받은 traceId를 동기화하여 TraceStatus를 생성하는 코드를 확인할 수 있다.
```java

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final TraceV2 trace;

    public String create(TraceId traceId, String name) {
        TraceStatus status = trace.beginSync(traceId, "UserService.create()");

        try{
            if (name.equals("ex")) {
                throw new IllegalStateException("예외 발생");
            }
            Users createdUser = Users.builder()
                    .username(name)
                    .build();
            userRepository.save(createdUser);
            trace.end(status);
            return "created";
        }catch (IllegalStateException e){
            trace.exception(status, e);
            throw e;
        }
    }
}

```

## 결과

depth에 대한 level 설정을 해줬기에 Service로 들어가면 level이 1 증가하고 로그도 Prefix와 함께 출력되는 것을 확인할 수 있다.

```shell

2024-12-25T18:49:15.387+09:00  INFO 3087 --- [nio-8080-exec-2] c.e.loggingtest.global.trace.TraceV2     : [72235a4a] UserController.create()
2024-12-25T18:49:15.387+09:00  INFO 3087 --- [nio-8080-exec-2] c.e.loggingtest.global.trace.TraceV2     : [72235a4a] |-->UserService.create()
2024-12-25T18:49:15.425+09:00  INFO 3087 --- [nio-8080-exec-2] c.e.loggingtest.global.trace.TraceV2     : [72235a4a] |<--UserService.create() time=38ms
2024-12-25T18:49:15.425+09:00  INFO 3087 --- [nio-8080-exec-2] c.e.loggingtest.global.trace.TraceV2     : [72235a4a] UserController.create() time=38ms

```


## 문제점
하지만 코드와 설명을 보면서도 느꼈겠지만 유지보수에 매우 어려우며 이미 구현한 서비스의 파라미터를 모두 변경해야 한다는 어려움이 있다.
이러한 동기화 문제 및 유지 보수에 어려운 문제점들을 디자인 패턴을 사용해서 해결해보려고 한다.