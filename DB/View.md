# View

select 구문으로 조회한 테이블의 결과를 가상의 테이블로 간주하여 다양한 SQL구문을 적용할 수 있다.
이때 SubQuery를 사용해서 작성할 순 있지만 중복되는 쿼리가 많은 경우 번거롭고 효율이 떨어진다.

```mysql
SELECT result.username, result.age, table1.gender
FROM
    (SELECT user.username, user.age, user.gender
     FROM users, posts
     WHERE users.user_id = posts.user_id)
 WHERE result.username = 'LEE';
```

이런 번거로운 작업을 처리할 수 있는 효율적인 방법이 뷰이다.
SELECT 문의 결과로 만들어진 가상의 테이블이며 해당 뷰에 다양항 SQL질의를 수행해 볼 수 있다.

```mysql
CREATE VIEW view_name AS SELECT구문;

# 예시
CREATE VIEW myView AS
    SELECT user.usernmae, user.email, posts.title
    FROM users, posts
    WHERE users.user_id = posts.user_id;

```
뷰는 특정 사용자에게 테이블의 특정 데이터만을 보여주고자 할 때에도 사용할 수 있다. 해당 뷰릁 특정 사용자에 대해 접근 권한을 부여하면 된다.

## 뷰 사용시 주의점
뷰에 대한 조회에는 제한이 없지만, 삽입과 수정, 삭제등이 불가능할 수도 있다는 점이다.
특히 여러 테이블을 조회한 결과인 SELECT한 결과로 만들어진 뷰는 삽입/수정/삭제 연산이 제약 조건을 어기기 쉽기 때문이다.
그래서 뷰는 조회를 위한 목적으로 자주 사용된다.