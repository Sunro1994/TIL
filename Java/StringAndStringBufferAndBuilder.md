
----

| 분류   | String    | StringBuffer                    | StringBuilder        |
| ------ | --------- | ------------------------------- | -------------------- |
| 변경   | Immutable | Mutable                         | Mutable              |
| 동기화 |           | Synchronized 가능 (Thread-safe) | Synchronized 불가능. |

---

## String 특징
- new 연산을 통해 생성된 인스턴스의 메모리 공간은 변하지 않는다(불변성)
- Garbage Collector로 제거되어야 한다.
- 문자열 연산 시 새로 객체를 만드는 OverHead 발생
  - 문자열 연산 시 컴파일러가 내부적으로 new StringBuilder("xxx").append("yyy").toString()으로 변환시킨다.
- 객체가 불변하므로, 멀티쓰레드환경에서 동기화를 신경 쓸 필요가 없음
- 따라서, 문자열 연산이 적고, 조회가 많은 멀티쓰레드호나경에서 사용하기 좋다.


## StringBuffer, StirngBuilder
- 공통점
  - 문자열 연산 시 새로 객체를 생성시키지 않고, 크기를 변경시킨다.(가변성)
  - 두 클래스의 메서드가 동일하다.
  - Buffer라는 공간을 통해 문자열을 바로 추가하기 때문에 공간 낭비가 없고 속도가 빠르다.
  - 
- 차이점
  - String Buffer는 Thread-Safe하고(Synchronized키워드 존재) Builder는 안전하지 않다.

## Compact String
    - jdk 8까지 String 객체의 값은 char[] 배열로 되어져 있었지만, jdk 9부터는 기존 char[]에서 byte[]을 사용하여 String Compacting을 통한 성능 및 Heap 공간 효율을 높이도록 수정되었다.(2 -> 1byte)
    - 인덱스를 구할 때에도 Latin1 문자열인 경우 1바이트, UTF-16 문자열의 경우 2바이트 데이터를 읽는다.
