# Casting이란?
- 변수가 원하는 정보를 모두 갖고 있는 것
```java
int a  =0.1; //Error x
int b = (int) true; // Error o, boolean은 int로 캐스팅 불가
```
- 위 코드처럼 0.1이 Double형태이지만, int로 형변환 될 정보 또한 가지고 있음
- true는 int형이 될 정보를 가지고 있지 않음

# Casting은 왜 필요한가?
1. 다형성 : 오버라이딩된 함수를 분리해서 활용할 수 있다.
2. 상속 : 캐스팅을 통해 범용적인 프로그래밍이 가능하다.

# 형변환의 종류
1. 묵시적 형변환 : 캐스팅이 자동으로 발생(Upcasting)
```java
public class Parent {
    
}
public class Child extends Parent{
    
}

Parent p = new Child(); // 굳이 Parent로 형변환할 필요가 없다.
```

> Parent를 상속받은 Child는 Parent의 속성을 포함하고 있기 때문에 형변환이 필요하지 않다.


2. 명시적 형변환 : 캐스팅할 내용을 명시해야 하는 경우(다운 캐스팅)
```java
Parent p = new Child();
Child c = (Child)p; //다운캐스팅은 업캐스팅이 발생한 이후에 작용한다.
```

### 예시문제
```java
class Parent {
	int age;

	Parent() {}

	Parent(int age) {
		this.age = age;
	}

	void printInfo() {
		System.out.println("Parent Call!!!!");
	}
}

class Child extends Parent {
	String name;

	Child() {}

	Child(int age, String name) {
		super(age);
		this.name = name;
	}

	@Override 
	void printInfo() {
		System.out.println("Child Call!!!!");
	}

}

public class test {
    public static void main(String[] args) {
        Parent p = new Child();
        
        p.printInfo(); // 문제1 : 출력 결과는?
        Child c = (Child) new Parent(); //문제2 : 에러 종류는?
    }
}
```

### 문제 1 : Child Call
- 자바에서는 오버라이딩 된 함수를 동적 바인딩하기 때문에, Parent에 담겼어도 Child의 printInfo()함수를 불러오게 된다.

### 문제 2 : RuntimeError
- 컴파일 과정에서 데이터형의 일치만 따지기 때문에 별 문제가 없지만 런타임 과정에서는 Child 클래스에 Parent클래스를 넣을 수 없다는 것을 발견하게 되고 Runtime Error가 발생한다.