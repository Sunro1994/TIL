**\[요구사항\]**

1\. 소셜 회원가입 유저는 리다이렉트 하여 추가 정보 가입 페이지로 이동한다.(그 이전까지는 회원의 'isNewUser' 컬럼이 true)

2\. 추가 정보 가입 페이지 입력을 모두 마친 유저는 'isNewUser'컬럼이 false로 변경된다.

3\. 3개월 이상 추가 정보 가입을 마치지 않은 유저는 삭제 처리한다. (Soft Delete방식으로 'isDelete'가 true 처리)

**\[구현 코드\]**

**1\. application.yaml**

dataSource를 두 구개로 나눠 진행했다. 하나는 기존에 사용하던 데이터가 담겨 있는 DB이고, 하나는 배치 작업 과정이 담겨있는 메타 데이터 DB였다.

spring.batch.job = false로 하는 이유는 애플리케이션 초기화 단계에서 job으로 등록한 작업들을 수행할 것인지 묻는 것이기에 false로 작동되는 걸 막았다.

initialize-schema=always, schema = classpath:org/springframework/batch/core/schema-mysql.sql 이 설정은 배치에서 제공하는 관리 스키마를 자동으로 생성하도록 설정하는 구문이다. 이 위치는 외부 라이브러리 내부에 들어가면 확인할 수 있다.


```
spring:
  batch:
    job:
      enabled: false
    jdbc:
      initialize-schema: always
      schema: classpath:org/springframework/batch/core/schema-mysql.sql
  datasource-data:
    url: jdbc:mysql://localhost:3306/mentoapp?serverTimezone=Asia/Seoul
    jdbc-url: jdbc:mysql://localhost:3306/mentoapp?serverTimezone=Asia/Seoul
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: 아이디
    password: 비번
  datasource-meta:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/metamentoapp?serverTimezone=Asia/Seoul
    jdbc-url: jdbc:mysql://localhost:3306/metamentoapp?serverTimezone=Asia/Seoul
    username: 아이디
    password: 비번
```

**2\. DataConfig**

DataDBConfig는 yaml에서 설정한 내 데이터가 들어있는 DB에 관련하여 JPA를 설정한 파일이다.

**basePackages** 는 JPA가 관리할 엔티티의 영역설정

**entityManagerFactoryRef**는 엔티티 매니저 객체 이름

**transactionManagerRef**는 트랜잭션을 관리할 객체 이름이다.  각 이름과 빈으로 등록한 메서드의 이름이 동일해야 한다.

dataDBSource() 메서드에서 prefix를 설정해 준 이유는 해당 prefix의 뒤 설정을 해당 클래스에서 사용하겠다고 선언한 것이다.

아래에서 적을 MetaConfig파일과 분리하여 설정을 하기 위해 명시해 준다.

**\[주의\]**

yaml에서 설정한 내용들이 이 설정을 함으로써 충돌을 일으킬 수 있으므로 yaml에서 설정한 내용을  entityManager메서드 내부에서 설정해 준다. 내부의 packageScan영역또한 엔티티매니저가 관리할 JPA영역을 설정해준다.

각각의 의존성 연결은 코드를 보아도 알 수 있듯이 DataSource객체가  엔티티매니저와 연결되며 엔티티매니저는 트랜잭션매니저와 연결된다.

dataSource < entityManager < TransactionManger 이러한 관계를 가진다. 

```
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

**3\. MetaDBConfig**

위와 동일하게 prefix를 설정하여 Meta Data로 사용할 DB와 연결시킨다.

해당 DB는 Batch에 관환 스토리를 확인하는 DB이기에 별도의 설정 없이 트랜잭션매니저만 DataSource와 빈으로 생성해 준다.

중요한 점은 @Primary로 데이터 소스 우선순위를 명확히 설정해줘야 한다. 이 어노테이션을 적용하지 않으면 초기화 과정에서 DB연결 설정이 머신이 명확하지 않다는 예외를 발생시킨다.

```
@Configuration
public class MetaDBConfig {

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

}
```

**4\. Job**

**\- 구성**

작업에 필요한 객체는 Job, Step, Reader, Process, Writer 이렇게 5가지로 이루어져 있다.

**\- 의존성**

의존성으로 JobRepository, PlatformTransactionManager, 실제 내가 사용하고 있는 Repository가 필요하다. JobRepository를 사용하여 이전에 설정한 내 MetaDB에 작업이 트랜잭션 단위로 수행이 된다. Job실행 상태와 재실행여부를 판단하는 역할이다.

**\-Job**

Job객체에서 Step에 대한 의존성을 연결시키고 Step에서는 Read, Process, Writer에 대한 의존성을 연결시킨다.

Job은 하나의 단계가 아니라면 next를 사용하여 두 가지 스텝 그 이상을 연결할 수 있다.

**\-Step**

Step에서는 생성자로 name, Repository를 명시해야 한다. chunk 옵션을 사용해서 작업단위를 나눌 수 있으며  매개변수로는 작업할 사이즈, 트랜잭션매니저 객체가 필요하다. 이 트랜잭션 매니저로 작업에 대한 롤백/커밋이 이루어진다. 그 이후 reader, processor, writer에 각 객체를 집어넣어 주고 빌드시키면 된다.

**\-Reader**

Step의 빌드단계에 필요한 Reader를 먼저 보자. Reader를 통해 작업할 데이터의 읽기를 수행한다.

빌드 단계에서는 작업의 이름, 수행할 사이즈, 메서드 이름, 레포지터리, 정렬 등을 수행할 수 있다.

여기서 메서드 이름은 실제 레포지터리에 있는 메서드의 이름과 동일해야 한다. 레포지터리에는 의존성으로 연결한 내 실제 사용하는 해당 JPARepository 추상체를 넣어준다. 정렬은 실제 엔티티 컬럼 이름과 동일한 필드명을 지정해줘야 하며 두 번째 인자로 오름차순, 내림차순을 설정한다.

**\-Processor**

해당 객체는 읽어온 객체의 처리 과정을 명시하는 객체이다.

프로세서에서는 ItemProcessor객체를 생성하여 process 메서드를 구현한 구현체를 반환하는 작업을 거친다. 매개변수는 Object타입을 받아야 하므로 instanceOf를 사용하여 반환되는 객체를 처리해 줬다.

내 코드에서는 1달 기준으로 'isNewUser'필드가 true인 객체들을 모두 DELETE상태로 변경하는 작업을 수행한다.

**\- Writer**

마지막으로 처리한 내용을 쓰기 작업하는 객체이다. Process에서 작업한 item객체를 넘겨받아 실제 내 레포지터리에 작업을 저장하는 단계를 거친다. 데이터의 타입은 Object이기에 이 부분은 item에 대한 저장 과정을 익명 함수과정으로 처리했다.

```
@Configuration
@RequiredArgsConstructor
public class DeleteOldUserJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final UserJPARepository userRepository;

    @Bean
    public Job deleteOldUserFirstJob() {
        return new JobBuilder("DeleteOldUserJob", jobRepository)
                .start(deleteOldUserStep()) //step을 만들어 넣어야 하는 자리
                .build();
        //처음 시작할 step 다음 단계가 있다면 .nextStep사용
    }

    @Bean
    public Step deleteOldUserStep() {
        return new StepBuilder("deleteFirstStep", jobRepository)
                .chunk(10, transactionManager) //transaction을 관리해준다. 실패시 롤백해주거나 커밋해주는 역할
                .reader(beforeReader())
                .processor(afterProcessor())
                .writer(afterWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<UsersEntity> beforeReader() {
        return new RepositoryItemReaderBuilder<UsersEntity>()
                .name("ItemReader")
                .pageSize(10)
                .methodName("findAll")
                .repository(userRepository)
                .sorts(Map.of("userId", Sort.Direction.ASC))
                .build();

    }

    @Bean
    public ItemProcessor<Object, Object> afterProcessor() {
        return new ItemProcessor<>() {
            @Override
            public Object process(@Nullable Object item) throws Exception {
                if (item instanceof UsersEntity user) {
                    // 현재 날짜 기준으로 1개월 이전 날짜 계산
                    LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
                    // Date -> LocalDate 변환
                    LocalDate createdDate = user.getCreatedAt()
                            .toInstant(ZoneOffset.UTC)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    if (!user.isDeleted() && user.isNewUser()&&createdDate.isBefore(oneMonthAgo)) {
                        user.setDeleted(true);
                        user.setAccountStatus(AccountStatus.DELETED);

                    }
                    return user;
                }
                return item;
            }
        };
    }


    @Bean
    public ItemWriter<Object> afterWriter() {
        return items -> {
            for (Object item : items) {
                if (item instanceof UsersEntity) {
                    userRepository.save((UsersEntity) item);
                    userRepository.flush();
                } else {
                    throw new IllegalArgumentException("Unsupported item type: " + item.getClass().getName());
                }
            }
        };
    }


}
```

**5\. Schedule**

마지막으로 이러한 작업을 수행하는 Job을 주기적으로 처리하기 위한 Schedule객체를 생성한다.

Schedule에서는 jobLauncher와 JobRegistry를 사용하여 기존에 등록한 Job객체를 불러오고 실행시킬 수 있다.

보시다시피 하나의 메서드를 만들고 @Scheduled 어노테이션의 cron 속성으로 어떤 주기를 갖고 수행할 것인지 설정할 수 있다.

정확히 기억은 안 나지만 시/분/초/한 달 중 며칠/몇 달/무슨 요일 순이였던 것 같다. 내가 설정한 주기는 매달 1일 00:01에 작업을 수행하도록 설정하였다.

JobParameter는 생성일자를 파라미터로 함께 넣어주는 작업을 수행한다. JobParameter를 통해 유일성을 보장받을 수 있다.

이렇게 설정한 내용들을 최종적으로 jobLauncher.run() 메서드에 jobRegistry.getJob(내가 작성한 Job메서드 이름, jobParamter 변수)를 넣어 주기적으로 실행시킬 수 있다. 내가 작성한 Job메서드 이름은 이전 Job객체어 name 속성에 명시한 이름과 동일해야 한다. 

```

@Configuration
@Slf4j
public class FirstSchedule {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    public FirstSchedule(JobLauncher jobLauncher, JobRegistry jobRegistry) {
        this.jobLauncher = jobLauncher;
        this.jobRegistry = jobRegistry;
    }

    @Scheduled(cron = "1 0 0 1 * ?", zone = "Asia/Seoul")
    public void runFirstJob() throws Exception {

        log.info("유저 Batch 수행");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        String date = dateFormat.format(new Date());

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", date)
                .toJobParameters();

        jobLauncher.run(jobRegistry.getJob("DeleteOldUserJob"), jobParameters);
    }
}
```


**\[주의 사항\]**

1\. 모든 엔티티 필드의 @Column을 정확히 명시해줘야 한다. Hibernate는 기본적으로 변수명을 snake타입으로 변경해 주지만 DataDBConfig에서 직접 설정을 하게 되면 카멜케이스로 테이블의 컬럼명을 생성하기 때문에 해당 컬럼을 찾을 수 없다는 오류가 발생한다.

2\. JPA가 관리하는 엔티티를 사용하는 코드는 모두 영속성상태여야 한다. Hibernater가 자동으로 더티 체킹을 하고 영속성 상태의 관리를 해주지만 위 설정을 하는 순간 detach상태인 엔티티 객체들에 대해 예외가 발생한다.