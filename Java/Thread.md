# Java 에서의 Thread란

- 멀티 태스킹을 먼저 알아보자
  - 실제로 덩시에 처리될 수 있는 프로세스의 개수는 CPU 코어의 개수와 동일한데, 이보다 많은 개수의 프로세스가 존재하기 때문에 모두 동시에 처리할 수 없다.
  - 각 코어들은 그래서 아주 짧은 시간동안 여러 프로세스들을 번갈아가며 처리하는 방식을 통해 동시에 작업하는 것처럼 보인다.
  - 마찬가지로 멀티 쓰레딩또한 하나의 프로세스안의 여러개의 스레드가 동시에 작업을 수행하는 것을 말한다. 



## 쓰레드 구현

- 자바에서 쓰레드 구현 방법은 2가지가 있다.
  - Runnable Interface 구현
  - Thread 클래스 상속
- 두 방식 모두 run()메서드를 오버라이딩하는 방식이다.
```java
public class TestThread implements Runnabe{
    @Override
    public void run(){
        // workCode...
    }
}

public class TestThread2 implements Thread{
    @Override
    public void run(){
        // workCode....
    }
}
```


## 쓰레드 생성

- Runnable Interface를 구현한 경우는, 해당 클래스를 인스턴스화해서 Thread 생성자에 인자로 넘겨줘야한다.
- 그리고 run()을 호출하면 Runnable 인터페이스에서 구현한 run()이 호출되므로 오버라이딩하지 않아도 된다는 장점이 있다.
- Thread 클래스를 상속받은 경우, 상속받는 클래스 자체를 스레드로 사용할 수 있다.
- Thread 클래스를 상속받으면 쓰레드 클래스의 메서드(getName())을 바로 사용할 수 있지만, Runnable의 경우 Thread클래스의 static메서드인 currentThread()를 호출하여 현재 스레드에 대한 참조를 얻어와야만 호출이 가능하다.
```java
public class ThreadTest implements Runnable {
    public ThreadTest() {}
    
    public ThreadTest(String name){
        Thread t = new Thread(this, name);
        t.start();
    }
    
    @Override
    public void run() {
        for(int i = 0; i <= 50; i++) {
            System.out.print(i + ":" + Thread.currentThread().getName() + " ");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
```


## 쓰레드 실행
- 쓰레드의 실행은 run()이 아닌 start()로 호출해야 한다.
- run()은 동일한 스레드에서 하나의 스택으로 호출하지만 start()는 새로운 스택 프레임을 생성하기 때문에 동시 작업이 가능하기 때문이다.
- run()을 사용한다는 것은 스레드 활용이 아닌 단순한 하나의 호출일 뿐이다.

## 쓰레드의 상태 5가지
1. new : 쓰레드가 생성되고 아직 start()가 호출되지 않은 상태
2. runnable : 실행 중 또는 실행 가능 상태
3. blocked : 동기화 블럭에 의해 일시정지된 상태(lock이 풀릴 때까지 기다림)
4. wating, time_wating : 실행가능하지 않은 일시정지 상태
5. terminated : 스레드 작업이 종료된 상태

## 쓰레드 동기화 방법
- 임계 영역(critical section) : 공유 자원에 단 하나의 스레드만 접근하도록함(하나의 프로세스에 속한 스레드만 가능)
- 뮤텍스(Mutex) : 공유 자원에 단 하나의 스레드만 접근하도록 함(서로 다른 프로세스에 속한 스레드도 가능)
- 이벤트(event) : 특정한 사건 발생을 다른 스레드에 알림
- 세마포어(semaphore) : 한정된 개수의 자원을 여러 스레드가 사용하려고 할 떄 접근 제한
- 대기 가능 타이머(waitable timer) : 특정 시간이 되면 대기 중이던 스레드 깨움

## 임계영역 설정
- 서로 다른 두 객체가 동기화를 하지 않은 메서드를 같이 오버라이딩해서 사용하면, 두 스레드가 동시에 진행되므로 원하는 값을 얻지 못한다.
- 그러므로 오버라이딩 되는 부모 클래스에 synchronized를 걸어주면 임계역영이 생성되며 해결할 수 있다.
```java
//synchronized : 스레드의 동기화. 공유 자원에 lock
public synchronized void saveMoney(int save){    // 입금
    int m = money;
    try{
        Thread.sleep(2000);    // 지연시간 2초
    } catch (Exception e){

    }
    money = m + save;
    System.out.println("입금 처리");

}

public synchronized void minusMoney(int minus){    // 출금
    int m = money;
    try{
        Thread.sleep(3000);    // 지연시간 3초
    } catch (Exception e){

    }
    money = m - minus;
    System.out.println("출금 완료");
}
```


## wait(), notify()
> 스레드가 서로 협력관계일 경우 무작정 대기시키는것은 올바르지 않기 때문에 사용하게 됨
- wait() : 쓰레드가 lock을 가지고 있으면, lock권한을 반환하고 대기하게 만듬
- notify() : 대기 상태인 쓰레드에게 다시 lock권한을 부여하고 수행하게 만듬
두 메서드는 동기화 된 영역(임계 영역)내에서 사용되어야 한다.
- 동기화 처리한 메서드들이 반복문에서 활용된다면 의도한 결과가 나오지 안흔다. 이때 wati(), notify()를 try-catch문에서 적절히 사용하여 해결할 수 있다.
```java
/**
* 스레드 동기화 중 협력관계 처리작업 : wait() notify()
* 스레드 간 협력 작업 강화
*/

public synchronized void makeBread(){
    if (breadCount >= 10){
        try {
            System.out.println("빵 생산 초과");
            wait();    // Thread를 Not Runnable 상태로 전환
        } catch (Exception e) {

        }
    }
    breadCount++;    // 빵 생산
    System.out.println("빵을 만듦. 총 " + breadCount + "개");
    notify();    // Thread를 Runnable 상태로 전환
}

public synchronized void eatBread(){
    if (breadCount < 1){
        try {
            System.out.println("빵이 없어 기다림");
            wait();
        } catch (Exception e) {

        }
    }
    breadCount--;
    System.out.println("빵을 먹음. 총 " + breadCount + "개");
    notify();
}
```

## Java 고유 락

1. Instrinsic Lock(=monitor lock)
   - Java의 모든 객체는 lock을 갖고있다. 그래서 synchronized키워드를 사용해서 동기화할 수 있다.
   ```java
public class Counter{
private Object lock = new Object(); // 모든 객체가 가능 (Lock이 있음)
private int count;

    public int increase() {
        // 단계 (1)
        synchronized(lock){	// lock을 이용하여, count 변수에의 접근을 막음
            return ++count;
        }
        
        /* 
        단계 (2)
        synchronized(this) { // this도 객체이므로 lock으로 사용 가능
        	return ++count;
        }
        */
    }
    /*
    단계 (3)
    public synchronized int increase() {
    	return ++count;
    }
    */
    }
    ```
2. Reentracy
   - Lock 획득을 위한 쓰레드가 같은 Lock을 얻기 위해 대기할 필요가 없는 것
   - Lock의 획득이 호출 단위가 아닌 Thread단위

## 가시성
- 여러 Thread가 동시에 작동하였을 때, 한 쓰레드가 쓴 값을 다른 쓰레드가 볼 수 있는지, 없는지 여부
- volatile을 사용해서 가시성을 보장받을 수 있다.
- 자바에서 스레드는 성능을 높이기 위해 주 메모리 (힙과 같이 모든 스레드가 공유하는 메모리) 에 대해 로컬 메모리( CPU 레지스터 또는 캐시) 에 복사해서 작업함.
- 이후 작업이 끝나면 주 메모리에 기록. 
- 따라서 다른 스레드가 주 메모리에 기록하기 전에는 최신값을 보지 못한다. 
- volatile 으로 선언하면, 모든 쓰기와 읽기가 주 메모리에서 이루어진다.

### Volatile의 장점
- 성능 최적화를 위해 프로그램의 최정 결과에 영향을 미치지 않는 범위에서 재정렬을 하게 되는데 재정렬을 방지하여 가시성 보장을 할 수 있다.
- 성능 최적화를 위한 생성자 호출 객체 초기화 후 변수에 할당이 아닌 역순으로 진행되는것을 방지한다.
- 하지만 동시성 문제가 발생할 수 있으므로 synchronized 블록이나 atomic 패키지의 클래스들을 사용해야 한다.