# SubQuery And Join

## SubQuery
다른 SQL문이 포함된 SQL문을 뜻하는 서브쿼리는 소괄호로 감싸 외부 쿼리와 내부쿼리를 구분할 수 있다.
SELECT문은 소괄호로 감싸진 서브 쿼리의 형태로, 다른 SELECT, INSERT, UPDATE, DELETE 문 안에 포함될 수 있다.
대표적으로 2가지 유형으로 사용한다.

- SELECT문 안에 SELECT문이 포함된 서브 쿼리
```mysql
SELECT
    USERS.USERNAME,
    (SELECT COUNT(*)
        FROM POSTS
        WHERE POSTS.USER_ID = USERS.USER_ID) AS POSTS_COUNT
    FROM USERS;
```
- DELETE문 안에 SELECT문이 포함된 서브 쿼리
```mysql
DELETE FROM POSTS
WHERE USER_ID=(
    SELECT USER_ID
    FROM USERS
    WHERE EMAIL LIKE '%naver.com%'
    );
```


## JOIN

서브 쿼리를 통해 복잡한 상황을 세밀한 SQL문으로 작성할 순 있지만 SQL문이 너무 복잡해 질 수 있다.
이때는 조인을 사용하는 것이 좋다.
조인에는 INNER JOIN, OUTER JOIN, FULL OUTER JOIN으로 구분된다.

### INNER JOIN
두 테이블이 조인 조건을 모두 만족하는 레코드를 결과로 반환한다

### LEFT OUTER JOIN
두 테이블중 왼쪽 테이블의 모든 레코드를 포함하고, 조인 조건을 만족하는 오른쪽 테이블의 레코드를 결과로 반환한다.
조건을 만족하지 않는 오른쪽 테이블의 필드는 NULL로 채워진다.

### RIGHT OUTER JOIN
LEFT OUTER JOIN의 반대로 작동된다.

### FULL OUTER JOIN
두 테이블의 모든 레코드를 포함하고, 조인 조건을 만족하지 않는 경우에는 상대 테이블 필드를 NULL로 채워 결과를 반환한다.
MySQL에서는 FULL OUTER JOIN을 지원하지 않으나 UNION을 사용하여 구현할 수 있다.
LEFT OUTER JOIN과 RIGHT OUTER JOIN을 UNION 키워드로 묶으면 된다.
```mysql
SELECT field1
from table1
left join table2 on join_condition
UNION
SELECT field1
from table1
         right join table2 on join_condition

```
