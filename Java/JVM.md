# JVM

## JVM이란?

JVM은 자바 바이트코드를 운영체제에 맞는 기계어로 변환하여 실행하는 역할이다. 운영체제와 독립적으로 수행이 가능하다.

## JVM 실행 과정

JVM은 자바 바이트코드를 운영체제에 맞는 기계어로 변환하여 실행하는 역할을 한다.
1. 자바 소스 코드는 먼저 바이트코드로 컴파일된다.
2. ClassLoader가 클래스를 로드하고, 실행 엔진이 해당 바이트 코드를 실행한다.
3. 실행 엔진은 인터프리터와 JIT 컴파일러 방식을 사용해 기계어로 변환하며, 필요시 가비지 컬렉션을 수행한다.

### JIT 컴파일러란?
자바 애플리케이션에서 자주 사용되는 코드를 미리 컴파일하여 기계어로 변환하고, 이후 해당 코드를 실행할 때 재사용한다.
인터프리터는 한 줄씩 해석하지만 JIT는 코드를 미리 번역하여 기계어로 변환하기
때문에 성능을 크게 향상시킬 수 있다.

### 바이트 코드란?
바이트 코드는 자바 소스 코드를 컴파일하여 생성되는 중간레벨 코드이다.
JVM이 실행할 수 있도록 설계된 이진 포맷으로, 기계어가 아닌 플랫폼에서 독립적인 명령어의 집합이다.


### 클래스 로더
JVM내에 클래스 파일을 동적으로 로드하고, 링크를 통해 배치하는 작업을 수행하는 모듈이다.
로드된 바이트 코드를 통해 JVM의 메모리 영역인 Runtime Data Area에 배치한다.

### Runtime Data Area

- Method(Static)영역
  - JVM에서 읽어들인 클래스와 인터페이스에 대한 런타임 상수 풀, 메서드와 필드, static 변수, 메서드 바이트 코드 등을 보관한다.
  - 여기서 Runtime Constant Pool은 클래스와 인터페이스 상수, 메서드와 필드에 대한 모든 레퍼런스를 저장한다.JVM은 런타임 상수 풀을 통해 해당 메서드나 필드의 실제 메모리 상 주소를 찾아 참조한다.
- Heap Area
  - 프로그램상에서 데이터를 저장하기 위해 런타임 시 동적으로 할당하는 사용하는 메모리 영역
  - New 연산자를 통해 생성한 객체, 인스턴스 또는 배열을 저장한다
  - 힙의 참조 주소는 스택이 갖고 있고, 해당 객체를 통해서만 힙 영역에 있는 인스턴스를 핸들링 할 수 있다.
- Stack Area
  - 메서드 호출 시 생성되는 스레드 수행정보를 기록하는 Frame저장, 메서드 정보, 지역변수, 매개변수, 연산 중 발생하는 임시 데이터가 저장 된다.
  - 기본 자료형을 생성할 때에도 저장된다.