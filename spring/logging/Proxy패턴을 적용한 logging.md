# Proxy를 사용한 Logging

우선 인터페이스 프록시와 상속을 받은 프록시 이렇게 두가지 방법으로 각 레이어에 프록시를 만들어 traceId를 출력하도록 만드는 프록시 객체를 만들어 
기존의 코드를 수정하지 않고도 프록시 객체가 실제 객체대신 수행되도록 만들 수 있다.
하지만 단점으로는 프록시 클래스를 너무 많이 만들어야 한다는 점이다. 또한 프록시 클래스들이 하는일은 로그를 다루는 일인다 그 로직이 모두 똑같다. 그렇다면 굳이 프록시 클래스를 여러개 만들 필요가 없는것이다.
이때 사용하는 것이 `동적 프록시`라는 기술이다.

자바가 기본적으로 제공하는 JDK 동적 프록시 기술이나 CGLIB 같은 프록시 생성 오픈소스 기술을 활용하면 프록시 객체를 동적으로 만들어 낼 수 있다.
하나의 프록시 객체를 생성하고 이를 동적으로 생성하는 기술을 사용할 것이다.

## JDK 동적 프록시

여기에는 자바의 리플렉션이라는 기술을 사용한다.
리플렉션은 클래스와 메서드의 메타데이터를 사용해서 애플리케이션을 동적으로 할당 가능하게 만들 수 있다는 장점이 있다.
하지만 런타임에 동작하기 때문에, 컴파일 시점에 오류를 잡을 수 없다. 따라서 프레임워크 개발이나 일반적인 공통적인 처리가 필요한 경우에 대해서만 주의해서 사용해야 한다.

### 리플렉션이란?

- 동적으로 클래스를 사용해야할 때 사용하는 기술
- 작성 싲머에는 어떤 클래스를 사용할지 모르지만 런타임 시점에서 가져와 실행해야 하는 경우
- IntelliJ의 자동완성 기능, 스프링 어노테이션기능등이 이에 속한다.
- 리플렉션을 통해서 Class, Constructor, Method, Field를 가져올 수 있다.

```java

public interface TempAInterface {
    String call();
}

public interface TempBInterface {
    String call();
}

@Slf4j
public class TempAImpl implements TempAInterface {
    @Override
    public String call() {
        log.info("A호출");
        return "data";
    }
}

@Slf4j
public class TempBImpl implements TempBInterface {
    @Override
    public String call() {
        log.info("B호출");
        return "data";
    }
}


```

위와 같이 중복된 작업을 수행하게되는 두 개의 구현체가 있다. 이를 동적 프록시로 처리해보자.

```java

import java.lang.reflect.Method;

public interface InvocationHandler {
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
}

@Slf4j
public class TimeInvocationHandler implements InvocationHandler {
    private final Object object;

    public TimeInvocationHandler(Object object) {
        this.object = object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("Proxy 수행");
        long startTime = System.currentTimeMillis();
        Object reuslt = method.invoke(object, args);
        long endTime = System.currentTimeMillis();
        log.info("Proxy 수행 시간 ={}", endTime- startTime);
        return endTime-startTime;
    }
}

```

보는바와 같이 invoke메서드 내부에서 Method의 invoke 메서드를 수행하여 메서드 이름과 매개변수를 넣어주어 실행시킨다.
이를 활용해서 동적으로 메서드를 작동시킬 수 있다.

```java
@Slf4j
public class JdkProxyTest{
    public static void main(String[] args){
        TempAInterface target1 = new TempAImpl();
        TimeInvocationHandler handler = new TimeInvocationHandler(target1);
        
        TempAInterface proxy = (TempAInterface) Proxy.newProxyInstance(TempAInterface.class.getClassLoader(), new Class[]{TempAInterface.class},handler );
        proxy.call();
        log.info("targetClass={}",target1.getClass());
        log.info("proxyClass={}", proxy.getClass());
    }
        }
```

이러한 방식을 사용해서 각 클래스에 특정한 메서드 이름이 매칭되는 경우 LogTrace로직이 실행되는 코드를 만들 수 있다.
하지만 여기에도 단점은 존재한다 인터페이스가 없으면 동적 프록시를 생성할 수 없다는 점이다.
각 레이어에 대해 인터페이스가 존재해야한다는 제한점이 있지만, 우리가 인터페이스를 생성하는 이유는 비즈니스 코드 또는 기술 스택에 변화가 있을 때 새로운 객체를 생성하고 요구사항에 맞춰 비즈니스 로직을 변경한뒤 갈아끼우기 위함이다. 동적 프록시를 위해 인터페이스를 강제로 만드는 상황이 발생할 수 도 있다는 것이다.
이 방법외에도 CGLIB이라는 바이트코드를 조작할 수 있는 특수한 라이브러리가 있지만 이를 우리가 직접 다뤄보기는 어려울 것 같았다. 다음에는 이를 개선하여 스프링에서 제공하는 AOP에 대해 알아보려고 한다.