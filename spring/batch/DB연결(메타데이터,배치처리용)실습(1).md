# 2개의 DB를 연결하기

- 하나의 메타데이터용 DB와 하나의 배치 처리 데이터용 DB를 준비한다.

## 환경
- Java 17
- Spring 3.x
- Spring JPA
- Spring Batch
- Lombok
- MySQL

## application.yaml

- 다음과 같이 환경설정을 미리 해준다.
- datasource를 구분해서 두 개의 설정을 해줘야하며 충돌을 방지하기 위해 하나는 `@Primary`어노테이션을 사용하여 먼저 초기화 되도록 설정해줄 것이다.
```
spring:
  batch:
    job:
      enabled: false
  datasource-data:
    url: jdbc:mysql://localhost:3306/스키마1?serverTimezone=Asia/Seoul
    jdbc-url: jdbc:mysql://localhost:3306/스키마1?serverTimezone=Asia/Seoul
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: 아이디
    password: 비밀번호

  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
    hibernate:
      ddl-auto: update
  data:
    redis:
      host: localhost
      port: 6379
  messages:
    basename: messages
    encoding: UTF-8
  datasource-meta:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/스키마2?serverTimezone=Asia/Seoul
    jdbc-url: jdbc:mysql://localhost:3306/스키마2?serverTimezone=Asia/Seoul
    username: 아이디
    password: 비밀번호
```


## DataDBConfig(MainDB)

주의해야할점은 `@EnableJpaRepositories`에서 명시한 속성값과 아래 `LocalContainerEntityManagerFactoryBean`의 메서드명은 꼭 일치해야 한다는 점이다.
`@EnableJpaRepositories`의 `basePackages`와 `LocalContainerEntityManagerFactoryBean`의 설정중 `Scan`범위 또한 JPA가 관리해야할 Entity가 포함된 범위로 잘 지정해줘야한다.

```java

@Configuration
@EnableJpaRepositories(
        basePackages = "com.mentit.mento",
        entityManagerFactoryRef = "entityManager",
        transactionManagerRef = "dataTransactionManager"
)
public class DataDBConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource-data")
    public DataSource dataDBSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManager() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataDBSource());
        em.setPackagesToScan("com.mentit.mento");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        HashMap<String,Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.show_sql", "true");
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean
    public PlatformTransactionManager dataTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManager().getObject());
        return transactionManager;
    }
}
```



## MetaDBConfig

`@Primary`를 명시하여 우선적으로 스캔할 수 있도록 꼭 설정해줘야 한다.
`@ConfigurationProperties`를 사용해서 어떤 프로퍼티를 가져와야 하는지도 명시해준다.

```java
 @Primary
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource-meta")
    public DataSource metaDBSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager metaTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

```