# Partitioning And Sharding

## Partitioning

테이블은 수직적 또는 수평적으로 분할될 수 있다.
`수평적 분할`은 테이블의 행을 기준으로 테이블을 나누고, `수직적 분할`은 테이블의 열을 기준으로 테이블을 나누어 저장한다.

정규화된 테이블일지라도 물리적으로 테이블의 열을 분리하여 저장하는 것이 효율적일 때가 있다.
테이블에 발생하는 트랜잭션 수에 비해 테이블 내에 열이 과도하게 많거나, 특정 열에 속하는 레코드의 데이터 크기가 다른 열의 레코드에 비해 과도하게 큰 경우, 또는 보안 상의 이유로 특정 열을 별개의 테이블로 나누어 저장해야 하는 경우 수직적 분할이 필요하다.

예를 들어 게시글id, title, writer, content가 있는데 content에 과도하게 많은 양의 데이터가 있어 크기가 큰 경우에는 별도의 테이블로 분리한 뒤 필요할 때만 조회하는 것이 더 유리하다는 것이다.

`수평적 분할`을 하게 되는 경우는 수많은 레코드가 있고, 테이블의 레코드를 참조할때마다 모든 레코드를 한 번에 불러들여야하는 경우에는 수평적으로 분리한다.
대부분의 수평적 분할은 데이터베이스/테이블 분활, 데이터베이스/테이블 파티셔닝이라는 용어로 불리기도 한다.

### 수평적 분할 방법

1. 범위 분할
- 레코드가 가질 수 있는 범위를 정의하고, 해당 범위를 기준으로 테이블을 나눈다.
- 예를 들어 19990년, 2000년, 2010년으로 범위를 정의하고 범위에 따라 테이블을 나누는 방법도 있다.

```mysql
CREATE TABLE users(
    username varchar(50) not null ,
    registration_date DATE not null 
)

PARTITION BY RANGE (YEAR(registration_date)) (
    PARTITION p0 VALUES LESS THAN (1990),
    PARTITION p1 VALUES LESS THAN (2000),
    PARTITION p2 VALUES LESS THAN (2010),
    PARTITION p3 VALUES LESS THAN (2020),
    PARTITION p4 VALUES LESS THAN MAXVALUE 
    )
```

2. 목록 분할
- 레코드 데이터가 특정 목록에 포함된 값을 가질 경우 별도의 테이블로 분할한다.
- 예를 들어 테이블의 주소를 기준으로 특정 목록이 포함된 경우로 분할 할 수 있다.

```mysql
CREATE TABLE customer(
    name varchar(50),
    address varchar(100)
)
PARTITION BY LIST COLUMNS (address)(
    PARTITION p0 VALUES IN('Seoul', 'Busan', 'Daegu'),
    PARTITION p1 VALUES IN('Incheon','Gwangju','Daejeon')
    )
```

3. 해시 분할
- 특정 열 데이터에 대한 해시 값을 기준으로 별도의 테이블로 분할한다.
- 파티션별 레코드가 해시 값을 기준으로 균등하게 분배된다.
- 예를 들어 학생들의 전공 과목 데이터에서 어떠한 열ㅡㄹ 기준으로 분할하고, 레코드는 분할한 열에 해시 함수를 적용하여 생성된 해시 값에 따라 4개의 파티션으로 나뉘어 저장한다.
```mysql
CREATE TABLE students(
    id INT NOT NULL ,
    name varchar(30),
    major_id INT
)
PARTITION BY HASH ( major_id ) PARTITIONS 4;
```

4. 키 분할
- 키를 기준으로 별도의 테이블로 분할한다. 파티션별 레코드가 키를 기준으로 균등하게 분배된다.

```mysql
# 키 id를 기준으로 파티셔닝, id값에 따라 2개의 파티션으로 나뉜다.
CREATE TABLE students(
    id INT NOT NULL PRIMARY KEY ,
    name varchar(50)
)
PARTITION BY KEY ()
PARTITIONS 2;
```