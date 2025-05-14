# Serialization(직렬화)

- 직렬화된 데이터들은 모두 Primitive Type(기본형)이 되고, 이는 파일 저장이나 네트워크 전송 시 파싱이 가능한 유의미한 데이터가 된다.
- 따라서, 전송 및 저장이 가능한 데이터로 만들어주는 것이 바로 **직렬화(Serialization)** 이라고 말할 수 있다.
- 자바에서는 간단히 java.io.serializable Interface로 구현 가능하다.
- 직렬화 대상 : 인터페이스 상속 받은 객체, Primitive 타입의 데이터

## 직렬화 대상

- JVM에 상주하는 객체 데이터를 영속화 할 때
- Servlet Session
- Cache
- JAVA RMI(Remote Method Invocation)

```java

@Entity
@AllArgsConstructor
@toString
public class Post implements Serializable {
private static final long serialVersionUID = 1L;
    
private String title;
private String content;

```

## 직렬화 시 serialVersionUID를 적는 이유
- 선언하지 않아도 자동으로 해시 값이 붙지만 기존 클래스의 멤버 변수가 변경되면 SerialVersionUID가 변경되어 역직렬화 시 달라진 값으로 Exception이 발생할 수 있다.
- 따라서 SerialVersionUID를 직접 관리해야 Exception이 발생하지 않는다 다만 데이터가 누락될 수 있다.

## 문제 1. String으로 들어온 JsonData를 읽고 Map에 저장하기

```java
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GsonJsonObjectExample {
    public static void main(String[] args) {
        String data = "{\"name\" : \"LEESUNRO\" , \"age\" : 15, \"message\" : \"hello\"}";

        JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();

        String name = jsonObject.get("name").getAsString();
        int age = jsonObject.get("age").getAsInt();
        String message = jsonObject.get("message").getAsString();

        System.out.println("name: " + name);
        System.out.println("age: " + age);
        System.out.println("message: " + message);
    }
}
```


```java
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class JacksonExample {
    public static void main(String[] args) throws Exception {
        String data = "{\"name\" : \"LEESUNRO\" , \"age\" : 15, \"message\" : \"hello\"}";

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.readValue(data, Map.class);

        System.out.println(map);

        // age를 int로 꺼내고 싶을 때 (double -> int 변환)
        int age = ((Number) map.get("age")).intValue();
        System.out.println("age: " + age);
    }
}
```

