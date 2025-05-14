# Record

- 일반적인 클래스라면 클래스, 필드명, 생성자, getter를 추가하는 것이 일반적이지만
- Record를 사용하면 더 간결하게 사용할 수 있다.

```java
public class Person {
   private final String name;
   private final int age;
 
   public Person(String name, int age) {
      this.name = name;
      this.age = age;
   }
 
   public String getName() {
      return name;
   }
 
   public int getAge() {
      return age;
   }
}
```

## 장점
- 자동으로 필드를 private final로 생성한다.
- 생성자와 getter 또한 암묵적으로 생성해준다.
- equals, hashCode, toString도 자동으로 생성해준다.
- 대신 getter 메서드의 경우 구현시 getXXX보다 name(),age()와 같은 필드명으로 생성한다.

```java
public record Person(
	String name,
    int age
) {}
```