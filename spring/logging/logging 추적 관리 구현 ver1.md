# Loggin에 TraceId를 부여하여 추적을 쉽게 만들기

## 목차
- [요구 사항](#요구-사항)
- [TraceID 클래스](#traceid-클래스)
- [TraceStatus 클래스](#tracestatus-클래스)
- [TraceV1](#tracev1)
- [Test](#test)
    - [Controller](#controller)
    - [Service](#service)
- [결과](#결과)
- [문제점](#문제점)

---


우선 내 로그 기록을 살펴보면서 어떤 개선이 필요한지 찾아보자

```shell

select u1_0.uuid,u1_0.account_status,u1_0.address1,u1_0.address2,u1_0.birthday,u1_0.birthyear,u1_0.email,u1_0.fcm_token,u1_0.mapx,u1_0.mapy,u1_0.mate_mate_uuid,u1_0.name,u1_0.nickname,u1_0.owner_uuid,u1_0.password,u1_0.phone_number,u1_0.profile_image,u1_0.role,u1_0.user_gender from users u1_0 where (u1_0.account_status = 'ACTIVE') and u1_0.uuid=?
select u1_0.uuid,u1_0.account_status,u1_0.address1,u1_0.address2,u1_0.birthday,u1_0.birthyear,u1_0.email,u1_0.fcm_token,u1_0.mapx,u1_0.mapy,u1_0.mate_mate_uuid,u1_0.name,u1_0.nickname,u1_0.owner_uuid,u1_0.password,u1_0.phone_number,u1_0.profile_image,u1_0.role,u1_0.user_gender from users u1_0 where (u1_0.account_status = 'ACTIVE') and u1_0.uuid=4;
2024-12-24 06:43:11 INFO  p.t.d.chat.service.ChatServiceImpl - 유저의 마지막 세션 종료 시간 추출
2024-12-24 06:43:11 INFO  p.t.d.chat.service.ChatServiceImpl - User last disconnect time found: 2024-12-24 05:50:28.102
2024-12-24 06:43:11 INFO  p.t.d.chat.service.ChatServiceImpl - Unreceived messages fetched for roomId: 2. Count: 3
2024-12-24 06:43:11 INFO  p.t.global.logging.LoggingAspect - return type = ResponseEntity
2024-12-24 06:43:11 INFO  p.t.global.logging.LoggingAspect - return value = <200 OK OK,[ChattingResponseDto(lastTime=2024-12-24 06:42:58.0, roomId=2, userId=3, content=후, image=), ChattingResponseDto(lastTime=2024-12-24 06:24:10.0, roomId=2, userId=3, content=메시지좀 보내보쇼, image=), ChattingResponseDto(lastTime=2024-12-24 06:15:29.0, roomId=2, userId=3, content=바보, image=)],[]>
2024-12-24 06:43:11 INFO  p.t.g.w.WebSocketEventListener - WebSocket 연결 이벤트 수신: Session ID = wujehrnr, Authorization 헤더 = eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJnbnNkdWR3a2RAbmF2ZXIuY29tIiwiaWQiOjQsImF1dGgiOiJNRU1CRVJfS0FLQU8iLCJleHAiOjE3MzUwMjYwOTF9.pexCQdOXOFLlROFOVVEmUqNVsiTSfNDKnBdjKkKKqmU
2024-12-24 06:43:11 INFO  p.t.g.w.WebSocketEventListener - WebSocket 연결: User ID = 4, Session ID = wujehrnr
2024-12-24 06:43:11 INFO  p.t.g.w.WebSocketEventListener - Redis에 사용자 매핑 완료: user:session:4 -> wujehrnr, session:user:wujehrnr -> 4
2024-12-24 06:43:11 INFO  p.t.g.w.WebSocketEventListener - WebSocket 연결됨: 세션 ID = wujehrnr
2024-12-24 06:43:18 INFO  p.t.d.chat.service.ChatServiceImpl - 4 send This Message - 안녕
2024-12-24 06:43:18 INFO  p.t.d.chat.service.ChatServiceImpl - message contain Image : 
2024-12-24 06:43:18 INFO  p.t.d.chat.service.ChatServiceImpl - Finding chat room by ID: 2

```

보는 바와 같이 어떤 값들이 오고 가는지 확인은 가능하지만 각 레이어의 연결이 어떻게 진행되는지 확인할 수 없다.(토큰값은 확인용으로 출력해놓은 상태)
각 레이어들 간의 호출들을 하나의 Id값으로 묶어서 호출 순서 및 어디에서 어떤 예외가 발생하는지 더 효율적으로 볼 수 있도록 정리할 예정이다.
그 과정에서 요구사항은 다음과 같다.

## 요구 사항
1. public 메서드들의 호출에 대한 로그를 출력한다.
2. 로깅 출력과정은 비즈니스 로직에 영향을 줘서는 안된다.
3. 메서드 수행 시간이 함께 출력된다.
4. 정상과 예외 로그 처리를 분리한다.
5. 메서드의 호출 depth를 설정한다.
6. TraceId를 부여하여 하나의 HTTP요청 단위를 구분할 수 있다.

위 요구사항은 기본적인 구현후 문제점을 파악하고 해결하며 점점 클래스를 업그레이드할 것 이기에 버전별로 나눠서 클래스를 생성할 것이다.

## TraceID 클래스

```java

import lombok.Getter;

import java.util.UUID;

@Getter
public class TraceId {
    private String traceId;
    private int level;

    public TraceId() {
        this.traceId = createTraceId();
        this.level = 0;
    }

    public TraceId(String traceId, int level) {
        this.traceId = traceId;
        this.level = level;
    }

    private String createTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

}
```

로깅의 HTTP 요청을 단위별로 묶을 TraceId를 하나의 객체로 생성해준다. ID값은 유니크한 값이여야 하기에 생성자에서 UUID를 사용하여 무작위 값을 추출해 설정해주도록한다.
level은 depth를 의미한다. 초기화 단계에서 0부터 시작하도록 지정해준다.

## TraceStatus 클래스
```java

import lombok.Getter;

@Getter
public class TraceStatus {
    private TraceId traceId;
    private Long startTime;
    private String message;

    public TraceStatus(TraceId traceId, Long startTime, String message) {
        this.traceId = traceId;
        this.startTime = startTime;
        this.message = message;
    }
}

```

Trace의 상태를 관리하는 객체이다. TraceId 객체와 startTime과 message를 매개변수로 생성자를 가진다.

## TraceV1

```java
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TraceV1 {

    private static  final String START_PREFIX= "-->";
    private static final String END_PREFIX = "<--";
    private static final String EXCEPTION_PREFIX = "<X-";

    public TraceStatus begin(String message) {
        TraceId traceId = new TraceId();
        Long startTime = System.currentTimeMillis();

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

begin, end, exception 세 개의 public method를 갖고 있는 TraceV1 클래스다.
해당 클래스를 각 레이어마다 의존성을 부여하여 로깅을 출력할 수 있도록 만들었다.
각 레이어의 호출시작시에 begin 메서드를 사용하여 로그를 출력할 수 있으며 정상적으로 수행이 종료된 경우 end, 예외가 발생한경우 catch 구문에서 exception 메서드를 실행시켜
로그에 대해 정상/예외를 구분하여 확인할 수 있다.
또한 addSpace 메서드를 사용하여 depth를 가시적으로 볼 수 있도록 prefix와 구분하는 '|    '를 출력하도록 설정했다.

## Test

### Controller
API가 호출되면 trace.begin 메서드가 수행되면서 시작을 알리는 로그가 출력된다.

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
             answer = userService.create(name);
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
userService에서도 trace.begin메서드가 메서드의 호출 및 시작을 알리는 로그를 수행한다. 만약 받은 파라미터의 이름이 ex이면 예외를 출력하는 로그가 발생하며 정상 작동한 경우에는 종료되었음을 알리는 log와 created라는 결과값은 반환한다.

```java

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final TraceV1 trace;

    public String create(String name) {
        TraceStatus status = trace.begin("UserService.create()");

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
보는 바와 같이 traceId값과 메서드 수행 과정, 작업 시간을 확인할 수 있다. 
```shell
2024-12-25T18:35:54.737+09:00  INFO 2940 --- [nio-8080-exec-2] c.e.loggingtest.global.trace.TraceV1     : [5a2dfa9c] UserController.create()
2024-12-25T18:35:54.737+09:00  INFO 2940 --- [nio-8080-exec-2] c.e.loggingtest.global.trace.TraceV1     : [631d51ad] UserService.create()
2024-12-25T18:35:54.769+09:00  INFO 2940 --- [nio-8080-exec-2] c.e.loggingtest.global.trace.TraceV1     : [631d51ad] UserService.create() time=32ms
2024-12-25T18:35:54.769+09:00  INFO 2940 --- [nio-8080-exec-2] c.e.loggingtest.global.trace.TraceV1     : [5a2dfa9c] UserController.create() time=32ms
```

## 문제점
하지만 각 traceId가 일관적이지 않음을 확인할 수 있다. 
각 단계에서 new 연산자를 통해 TraceId값을 새로 생성하기 때문이다. 이 값들을 동기화 해 줄 필요가 있다.
또한 addSpace()메서드의 level별로 '|'를 구분하였으나 depth에 대한 level이 증감되지 않으므로 이를 처리할 메서드를 작성해보려고 한다.
이 다음 내용은 [logging 추적 관리 구현2.md](logging%20추적%20관리%20구현%202.md)에서 이어진다.