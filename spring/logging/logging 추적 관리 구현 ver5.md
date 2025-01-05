# Logging Trace V5

## 이전 문제점
각각의 Controller, Service, Repository에서 주요 비즈니스 로직(CRUD)이 있고, 이 활동을 확인하기 위한 부가 기능인 로깅 출력 기능을 구현했다.
부가기능과 핵심 기능이 한 군데 모여 있어 가독성이 떨어지고, 코드가 복잡해지면 유지 보수하기가 어려워진다는 문제점이 발생한다.
핵심 기능과 부가 기능을 분리하여 설계하는 것이 디자인적으로 더 좋은 설계라고 할 수 있다.

```java
try{
             answer = userService.create(name); //핵심 기능
            trace.end(status); //부가기능
        }catch (Exception e){
            trace.exception(status, e); //부가기능
            throw e;
        }
```

## Template Method

템플릿 메서드 패턴은 동일한 코드의 중복을 최소화 하기 위해 전체적인 구조는 동일하게 작성하지만 부분적으로 다른 구문으로 구성된 메소드의 변경을 처리하는 것이다.
모든 계층에서 사용하는 begin,end,exception 메서드를 추상 클래스를 구현해두고, 핵심 기능들은 추상 클래스에 선언을 해놓은뒤 이 추상 클래스를 각각 상속받아 적용하는 방식이다.

예를 들어, 하나의 아이폰에서 전화 통화, 음량 조절, 온/오프 기능등은 동일하기 때문에 부모 클래스에서 이를 구현하며 동일한 기능이지만 다르게 작동하는 헬스 기능이라거나 각각의 모바일에서 다르게 작동할 수 있는
부분은 선언만 해놓고 자식 클래스에서 구현하도록 셋팅만 해두는 방식이다.

### 예시 코드
부가 기능 - 작업 시간 출력
핵심 기능 - coreLogic()메서드

coreLogic()은 직접 구현해서 사용해야 하며, 부가기능은 추상 클래스에서 이미 구현시켜놓은 모습이다.
```java
@Slf4j
public abstract class AbstractTempate{
    public void process(){
        long startTime = System.currentTimeMillis();
        //비즈니스 로직 수행
        coreLogic();
        // 비즈니스 로직 종료
        long endTime = System.currentTimeMillis();
        log.info("Time = {}",endTime-startTime);
    }
    
    protected abstract void coreLogic();
}
```

실제 AbstractTemplate를 상속받은 자식 클래스는 process만 구현하면 되는 장점을 확인할 수 있다.
코드를 여러번 사용하지 않는 경우에는 상속받는 방법외에 익명 내부 클래스를 사용하여 구현하는 방법도 있다.
```java
@Slf4j
public class SubClass extends AbstractTemplate{
    @Override
    protected void prcess(){
        log.info("비즈니스 로직 구현");
    }
}
```


## 적용
실제 내 코드에 해당 Template을 적용해보자
핵심 로직은 동일하게 coreLogic으로 선언부만 정의해놓은 미구현 상태로 셋팅해둔다.
그 외 부가 기능인 로그 출력 메서드는 추상클래스에서 모두 구현해둔다.
```java

@RequiredArgsConstructor
public abstract class AbstractMethodTemplate<T> {
    private final LogTrace trace;

    public T prcess(String msg){
        TraceStatus status = null;
        try {
            status = trace.begin(msg);
            T result = coreLogic();
            trace.end(status);
            return result;
        } catch (Exception e) {
            trace.exception(status, e);
            throw e;
        }
    }

    protected abstract T coreLogic();
}
```
```java

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/user")
public class UserControllerV2 {
    private final UserService userService;
    private final LogTrace trace;

    @GetMapping
    public String createUser(@RequestParam String name) {
        AbstractMethodTemplate<String> template = new AbstractMethodTemplate<String>(trace) {
            @Override
            protected String coreLogic() {
                return userService.create(name);
            }
        };

        return template.prcess("UserService.createUser()");
    }
}
```

## 문제점

하지만 여기서도 문제점이 발생한다.
부모 클래스와 자식 클래스 사이에는 강한 의존성이 발생한다. 자식 클래스에서는 execute 메소드를 사용하지 않지만, 부모 클래스의 execute()를 알아야 부가 기능을 실행 시킬 수 있다느 ㄴ것이다.
그리고 패턴 적용을 했으나 가독성이나 유지 보수성이 그렇게 좋아졌다고는 할 수 없다.
상속의 단점(강한 의존성)을 제거할 수 있는 전략 패턴을 다음 글에서 알아보고 다시 한번 개선해보겠다.
