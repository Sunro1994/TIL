# SQL

## DDL

Data Definition Language 라고하며 네가지 대표적인 명령어가 있다.

### CREATE

데이터베이스, 테이블, 뷰, 인덱스, 사용자등 데이터베이스에서 관리될 수 있는 다양한 대상을 정의할 수있다.

MYSQL에서는 다음과 같이 특정필드에 제약 조건을 함께 명시도 할 수있다.

```mysql
create database TIL_DATABASE;

use TIL_DATABASE;

create table practice1(
    user_id int primary key auto_increment,
    name varchar(50) unique default 'human' not null ,
    age int not null ,
    registration_dtae timestamp default current_time,
    birthdate date
);

# 다음 테이블을 생성할 때 user_id를 fk로 참조할 수 있다.
    create table practice_posts(
                                       posts_id int primary key auto_increment,
            user_id int,
            title varchar(100) not null ,
            content text,
            foreign key (user_id) references practice1(user_id)
    );

```

### 테이블의 조회

```mysql
DESCRIBE  practice1;
DESC practice1;
```

### 데이터베이스의 전체 테이블 조회

```mysql
SHOW Tables;
SHOW TABLES FROM TIL_DATABASE;
```

## ALTER

CREATE TABLE을 통해 생성된 테이블에 새로운 필드를 추가하거나 기존의 필드를 수정/삭제할 수 있으며, 제약 조건도 수정/삭제, 추가가 가능하다.

다음과 같은 예시를 통해 데이터 또는 제약조건의 추가/수정/삭제 예시를 확인할 수 있다.

```mysql

# 새로운 필드 추가
ALTER TABLE POSTS ADD COLUMN NEW_FIELD VARCHAR(50) UNICODE;

# 기존 필드 수정
ALTER TABLE POSTS CHANGE COLUMN NEW_FIELD NEW_FIELD2 VARCHAR(40) NOT NULL;

# 기존 필드 삭제
ALTER TABLE POSTS DROP COLUMN NEW_FIELD2;

# 외래 키 제약조건 추가
ALTER TABLE POSTS ADD FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID);

# 제약 조건 추가
ALTER TABLE POSTS ADD UNIQUE (NICKNAME);

# 제약 조건 수정
ALTER TABLE POSTS MODIFY TITLE VARCHAR(200) NOT  NULL ;

# pk 설정
ALTER TABLE POSTS ADD PRIMARY KEY (POST_ID);
```

## DROP

```mysql
DROP DATABASE POSTS;
DROP TABLE POSTS;
```


## DELETE, TRUNCATE, DROP의 차이

- DELETE
  - 특정 테이블에서 행을 삭제한다.
  - 트랜잭션 내에서 롤백이 가능하며 조건에 따른 선택적 삭제가 가능하다
  - 조건에 따른 선택적 삭제가 가능하다
  - 큰 테이블에서 모든 행을 삭제하려고 할때 성능이 저하될 수 있다.
- TRUNCATE
  - 특정 테이블의 모든 데이터를 한 번에 삭제한다.
  - 테이블 구조는 그대로 유지하되, 테이블 자체는 비워진다.
  - 테이블에 대한 권한을 소유한 사용자만 사용가능하다
  - 속도가 DELETE보다 빠르다.
- DROP
  - 테이블, 뷰, 인덱스등의 모든 객체를 완전히 삭제한다
  - 테이블 구조와 데이터 모두 삭제한다.
  - 복구가 불가능하다


## DML
Data Manipulation Language 라는 뜻으로 가장 실무에서 자주 사용하는 명령어이다.
크게 SELECT, UPDATE, DELETE, INSERT가 있다.

### INSERT
새로운 레코드를 삽입하기 위한 명령어이다.
삽입할 값을 지정하지 않은 필드는 기본값으로 채워지거나 기본값이 없다면 NULL로 채워진다.
가장 중요한 점은 무결성 제약 조건을 지켜야 한다는 점이다. 
not null, unique와 같은 제약 조건이 명시된 고유 키에 중복값을 저장하는 경우 실행이 거부된다.
```mysql
INSERT INTO table_name(field1, field2) values(value1, value2)

#여러개의 값을 한번에 넣고 싶은 경우
INSERT INTO table_name(field1, field2, field3) values
   (value1 ,value2, value3),
   (value1 ,value2, value3),
   (value1 ,value2, value3),
   (value1 ,value2, value3)
```

### UPDATE , DELETE

update,delete 명령어에서 where절에서 사용할 수 있는 연산자가 있다.
`=, > , < , >=, <=, <>(다를경우 참), 조건식1 AND 조건식2, 조건식1 OR 조건식2, NOT 조건식, IN`
만약 참조된 레코드가 수정/삭제될 경우, 참조하는 레코드는 다음과 같이 동작할 수 있다.
- cascade : 참조되는 데이터도 함께 수정/삭제
- set null : 참조하는 데이터를 null로 변경
- set default : 참조하는 데이터를 기본값으로 변경
- restrict : 수정/삭제를 허용하지 않음
- no action : 사실상 restrict와 동일한 작동
<br>
이러한 설정들은 create, alter 명령어의 제약조건으로 정의할 수 있다. 레코드가 수정될경우 on update 삭제될경우 on delete 뒤에 명시된다.

```mysql
UPDATE table_name
    set field1 = value1, field2 = value2
    where field_age < 12;
    
    
DELETE table_name FROM table
    where table_gender in ('man');
```


### SELECT

SELECT의 기본구조는 다음과 같다.
```mysql
SELECT FIELD1, FIELD2, AVG(FIELD_GRADE)
FROM TABLE_NAME
WHERE TABLE_NAME='TABLE'
GROUP BY FIELD1, FIELD2
HAVING AVG(FIELD_GRADE) >3.0
ORDER BY FIELD_GRADE DESC
LIMIT 3;
```

SELECT에서는 `패턴 검색`이 가능하다. 와일드카드인 '%','_'를 사용할 수 있는데 %는 0개 이상의 임의의 문자와 일치한다, _는 정확히 1개의 임의의 문자와 일치한다는 의미이다.

SELECT에서 사용하는 `GROUP BY`는 특정 필드를 기준으로 필드를 그룹화하여 사용하기 위해 사용된다.
주로 연산/집계 함수와 함께 사용된다.
```mysql
# A_CLASS의 과목별 학생수를 그룹화하여 조회하는 쿼리
SELECT SUBJECT, COUNT(*) AS STUDENT_COUNT
FROM A_CLASS
GROUP BY SUBJECT;
```

`HAVING`은 GROUP BY로 그룹화된 겨로가에 조건을 적용하기 위해 사용된다.
where절과 헷갈리지만, where절은 명시되는 조건식이 그룹화되기 전 개별 레코드에 적용되지만 having은 그룹화된 레코드에 대해 적용된다.

`ORDER BY`는 특정 필드를 기준으로 정렬하는데 사용된다. 기본값은 ASC(오름차순)이지만 DESC를 사용하여 내림차순으로도 정렬이 가능하다.


## TCL
Transaction Control Language에는 트랜잭션을 제어하는데 사용되는 명령어가 들어있다.
COMMIT, ROLLBACK, SAVEPOINT 이렇게 세가지 명령어가 있다.

### COMMIT
데이터베이스에 작업한 내용을 반영하는 명령어이다.
MySQL에서는 DDL문은 자동으로 커밋이 된다. 왜냐하면 auto acommit이 기본으로 켜져있기 때문이다. start transaction을 실행하거나 begin을 실행하면 자동 커밋이 꺼진 상태로 실행된다.
자동 커밋을 명시적으로 키거나 끌 수 있다.
```mysql
set autocommit =0; # 꺼짐
```

### ROLLBACK
데이터베이스에 작업한 내용을 취소하는 명령어이다. 취소한경우 해당 작업 이전 시점으로 되돌아간다.

### SAVEPOINT
ROLLBACK으로 돌아갈 시점을 지정하는 기능이다.
SAVEPOINT POINT_NAME 명령어는 되돌아갈 시점을 지정하는 명령어, ROLLBACK TO SAVEPOINT POINT_NAME은 해당 시점으로 되돌아가는 명령어이다.

## DCL

Data Control Language는 대표적으로 GRANT, REVOKE가 있다.
사용자에게 권한을 부여하거나 회수하는 명령어이다.
각각의 사용자마다 사용 가능한 SQL명령을 제한핟는 등의 권한을 부여하고 관리할 수 있다.