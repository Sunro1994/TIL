# AutoBoxing

- 기본 타입 데이터에 대응하는 Wrapper 클래스로 만드는 동작
- Wrapper 클래스 : Integer, Long, Float, Double, Boolean 등
- Integer, Long은 Java 9 버전 이후부터는 Deprecated 될 예정

# UnBoxing

- Wrapper클래스에서 기본 타입으로 변환
- 기본 타입 : int, long, float, double, boolean

## 특징

- JDK1.5 이상부터는 자바 컴파일러가 박싱과 언박싱이 필요한 상황에 자동으로 처리해준다.

```java
int i = 10;
Integer num = i;


Integer num = new Number(10);
int i = num;
```

- 편의성을 위해 제공되는 기능이지만 내부적으로 추가 연산을 거쳐야하기 때문에 최대한 동일한 타입의 연산이 이루어지도록 구현하는것이 좋다.
  - 래퍼 객체는 힙 메모리에 생성되기 떄문이다.
  - 스트림 API에서 IntStream, DoubleStream, mapToInt등 기본 타입 스트림을 활용하는 것도 좋은 방법이다.
      - 오토박싱 연산
        ```java
        public static void main(String[] args) {
        long t = System.currentTimeMillis();
        Long sum = 0L;
        for (long i = 0; i < 1000000; i++) {
            sum += i;
        }
        System.out.println("실행 시간: " + (System.currentTimeMillis() - t) + " ms");
        }

          // 실행 시간 : 19 ms
      
        public static void main(String[] args) {
        long t = System.currentTimeMillis();
        long sum = 0L;
        for (long i = 0; i < 1000000; i++) {
        sum += i;
        }
        System.out.println("실행 시간: " + (System.currentTimeMillis() - t) + " ms") ;
        }
    
          // 실행 시간 : 4 ms
          ```
        
- 불필요한 wrapper 클래스를 사용하면 NPE를 경험할 수도있다.
```java
public class BoxingNullExample {
public static void main(String[] args) {
Integer number = null; // 박싱된 타입에 null 할당

        // 언박싱 시도
        int value = number; // NullPointerException 발생
        System.out.println(value);
    }
}
```  