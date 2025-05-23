# Garbage Collector

## Garbage Collector란?

유효하지 않은 메모리인 가비지가 발생하게 되면 해당 메모리를 정리해야 다른 명령을 사용할 수 있다.
JVM에서 G.C를 사용해서 불필요한 메모리를 알아서 정리해준다.
Java에서 명시적으로 불필요한 확인하고 처리하기 위해서 일반적으로 null로 처리를 해준다.

## Minor GC와 Major GC

JVM의 Heap 영역은 처음 설계될 때 두 가지를 전제로 설계되었다.
- 대부분의 객체는 금방 접근 불가능한 상태가 된다.
- 오래된 객체에서 새로운 객체로의 참조는 아주 적게 존재한다
- 다시 말해서 대부분의 객체는 일회성이며, 메모리에 오랫동안 남아있는 경우는 드물다.
- 이 전제조건을 성립시키기 위해 Heap 영역이 존재하고 Young, Old 총 두 가지 영역으로 설계되었다.


### Young 영역(Young Generation)
- 새로 생성된 객체가 할당 되는 영역
- 대부분의 객체가 금방 Unreachable 상태가 되기 때문에, 많은 객체가 Young 영역에 생성되었다가 사라진다.
- Young 영역에 대한 G.C을 Minor GC라고 한다.

### Old 영역(Old Generation)
- Young 영역에서 Reachable 상태를 유지하여 살아남은 객체가 복사되는 영역
- Young 영역보다 크게 할당되며, 영역의 크기가 큰 만큼 가비지가 적게 발생한다.
- Old 영역에 대한 G.C를 Major GC라고 부른다.

### Old 영역이 Young 영역보다 크게 할당되는 이유
- 큰 객체들은 Young영역이 아니라 Old영역에 할당되기 때문이다. Young영역의 객체들은 수명이 짧기 때문이다.

### 카드 테이블
- Old 영역의 객체가 Young 영역의 객체를 참조하는 경우도 존재한다. 이를 대비하기 위해 Old 영역에는 512byte의 chunk로 되어 있는 카드 테이블이 존재한다.
- 카드 테이블에는 Old영역에 있는 객체가 Young 영역의 객체를 참조할 때 마다 그에 대한 정보가 표시된다.
- Young영역에서 가비지 컬렉션이 실행될 때 Old 영역에 존재하는 객체를 검사하여 참조되지 않는 Young영역의 객체를 식별하는 것이 비효율적이기 때문이다.
- 그래서 Young영역에서 가비지 컬렉션이 진행될 떄 카드 테이블만 조회하여 GC의 대상인지 식별할 수 있도록 한다.


## STOP THE WORLD

- 가비지 컬렉션을 실행하기 위해 JVM이 애플리케이션의 실행을 중지시킨다.
- GC가 실행될 때는 GC를 실행하는 쓰레드를 제외한 모든 쓰레드들의 작업이 중단된다.
- 만약 GC의 성능 개선을 위해 튜닝을 한다고하면 STOP THE WORLD의 시간을 단축시키는 것이다.

## MARK AND SWEEP
- Mark : 사용되는 메모리와 사용되지 않는 메모리를 식별하는 작업
- Sweep : Mark 단계에서 사용되지 않음으로 식별된 메모리를 해제하는 과정
- Stope The World를 통해 모든 작업을 중단시키면, GC는 스택의 모든 변수 , Reachable 객체를 스캔하여 어떤 객체를 참고하고 있는지 탐색한다.
- 이때 사용되고 있는 메모리를 식별(Mark), 사용되지 않는 객체들을 메모리에서 제거(Swep)한다.

## Minor GC의 동작 방식
- Eden 영역 : 새로 생성된 객체가 할당되는 영역
- Survivor 영역 : 최소 1번의 GC 이상 살아남은 객체가 존재하는 영역

1. 새로 생성된 객체가 Eden 영역에 할당
2. 객체가 계속 생성되어 Eden 영역이 꽉 차게 되면 Minor GC가 발생
   2.1 Eden 영역에서 사용되지 않는 메모리 해제
   2.2 Eden 영역에서 살아남은 객체는 1개의 Survivor 영역으로 이동
3. 1~2번의 과정이 반복되다가 한 개의 Survivor 영역이 가득 차게 되면 Survivor영역의 살아남은 객체를 다른 Survivor영역으로 이동시킨다.
4. 이러한 과정을 반복하여 계속 살아남은 객체는 생존 횟수가 Object Header에 age필드로 기록된다.
5. 이 age를 Object Header에서 조회하여 Old 영역으로 이동(promotion) 여부를 결정한다.

- 두 Survivor 영역중 반드시 한 영역은 비어있어야 한다.

### Hot Spot JVM -  bump the pointer
- 빠르게 Eden 영역에 객체를 할당하기 위해 Eden 영역에 마지막으로 할당된 객체의 주소를 캐싱해둔다. 다음에 새로운 객체의 유효 메모리를 탐색할 때 이 주소의 다음 주소에 바로 할당하여 속도를 높인다.
- 이때 Eden 영역에 적합한지만 판별하면 되므로 빠르게 할당 가능하다.

### Hot Spot JVM - TLABs(Thread-Local Allocation Buffers)
- 각각의 쓰레드마다 Eden영역에 객체를 할당하기 위한 주소를 부여하여 동기화 작업 없이 빠르게 메모리 할당이 가능하다.
- 각각의 쓰레드는 자신이 갖는 주소를 객체에 할당하여 동기화 없이 ㅂbump the pointer를 통해 빠르게 객체를 할당한다.

## Major GC의 동작 방식
- Young 영역에서 오래 살아남은 객체는 Old영역으로 이동(promotion)된다.
- 만약 계속 이동하여 Old영역의 메모리가 부족해지면 Major GC가 발생하게 된다.
- Old 영역은 Young영역보다 메모리 공간이 더 크기 떄문이 처리하는데 시간이 오래 걸리며, 10배 이상의 시간을 사용한다.



[GC더 자세히 알기](https://github.com/GimunLee/tech-refrigerator/blob/master/Language/JAVA/Garbage%20Collection.md#garbage-collection)


