# Java는 CallByValue인가?

## CallByValue
- 값에 의한 호출
- 함수가 호출될 때, 메모리 공간에서는 함수를 위한 별도의 임시공간이 생성된다. 또한 종료시 해당 공간이 사라진다.
- call by value 호출 방식은 함수 호출 시전달되는 변수 값을 복사해서 함수 인자로 전달한다.
- 이때 복사된 인자는 함수 안에서 지역적으로 사용되기 때문에 local value 속성을 가진다.

> 따라서 함수 안에서 인자 값이 변경되더라도, 외부 변수 값은 변경이 되지 않는다.

```java
void function(int n ) {
    n = 20;
}

void main(){
    int n =10;
    function(n);
    print(n);
}
```
> print로 출력되는 값은 그대로 10이 출력된다.


## callByReference
- 참조에 의한 호출
- call by reference 호출 방식은 호출 함수 시 인자로 전달되는 변수의 레퍼런스를 전달
- 따라서 함수 안에서 인자 값이 변경되면, Argumnet로 전달된 객체의 값도 변경된다.
```java
void function(int n){
    n = 20;
}

void main() {
    int n = 10;
    function(n);
    print(n);
}
```
> print로 출력되는 값은 20이된다.


## 따라서 Java는 Call By Value 방식이다.
- Call By Value의 경우 데이터 값을 복사해서 함수로 전달하기 때문에 원본의 데이터가 변경될 가능성이 없다. 하지만 인자를 넘겨줄 때마다 메모리 공간을 할당해야 학 때문에 메모리를 더 사용해야 한다.