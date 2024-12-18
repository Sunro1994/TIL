# Logback

## Logging을 하는 이유?
로깅이랑 시스템이 동작할 떄 시스템의 상태 및 동작 정보를 시간 경과에 따라 기록하는 것을 의미한다.
로깅을 통해 개발자는 개발 과정 혹은 개발 후에 발생할 수 있는 예상치 못한 애플리케이션의 문제를 진달할 수 있고, 다양한 정보를 수집할 수 있다.
하지만 로깅을 하는 단계에서 적절한 수준의 로그 기준을 잡지 못하면 불필요한 많은 양의 로그를 확인해야 한다.
결국 효율적인 로깅을 하는 방법을 이해하는 것이 중요하다.



## Logback이란?
Logback이란 Log4j를 기반으로 개발된 로깅라이브러리이다.
log4j에 비해 약 10배정도 더 빠른 퍼포먼스를 보여주며 메모리 효율성이 증대하는 장점이 있다.

## Logback 특징
- 로그에 특정 레벨을 설정할 수 있다.
- 실제 운영과 테스트 상황에서 각각 다른 출력 레벨을 설정하여 로그를 확인할 수 있다.
- 출력 방식에 대해 설정이 가능하다.
- 일정 시간마다 설정 파일을 스캔하여 어플리케이션 중단 없이 설정 변경이 가능하다.
- 별도의 프로그램 없이 자체적으로 로그 압축이 가능하다
- 로그 보관 기간 설정이 가능하다.

## Log Level
- Trace > DEBUG > INFO > WARN > ERROR
- ERROR : 로직 수행 중에 오류가 발생한 경우, 시스템적으로 심각한 문제가 발생하여 작동이 불가능한 경우
- WARN : 시스템 에러의 원인이 될 수 있는 경고 레벨, 처리가능한 상황
- INFO : 상태변경과 같은 정보성 메세지
- DEBUG : 어플리케이션의 디버깅을 위한 메세지 레벨, 개발 단계에서만 사용함
- TRACE : DEBUG 레벨보다 더 디테일한 메세지를 표현하기 위한 레벨, 개발 단계에서만 사용함


## Logback 설정파일의 속성들

### appender 종류
Log의 형태 및 어디에 출력할지 설정하기 위한 영역

- ConsoleAppender : 콘솔에 로그를 출력하는 속성
- FileAppender : 파일에 로그를 출력
- RollingFileAppender : 여러 개의 파일을 순회하며 로그를 저장
- SMTPAppender : 로그를 메일로 보냄
- DBAppender : 데이터베이스에 로그를 저장

### appender 속성
- encoder : pattern을 사용하여 원하는 형식으로 로그를 표현할 수 있다.
  - %Logger{length} : Logger Name
  - %-5level : 로그 레벨, -5는 출력의 고정폭 값
  - %msg : 로그 메세지 영역(==%message)
  - ${PID:-} : 프로세스 id
  - %d : 로그 기록 시간
  - %p : 로깅 레벨
  - %F : 로깅이 발생한 프로그램 파일명
  - %M : 로깅이 발생한 메소드의 이름
  - %I : 로깅이 발생한 호출지의 정보
  - %L : 로깅이 발생한 호출지의 라인 수
  - %thread : 현재 쓰레드 명
  - %t : 로깅이 발생한 Thread명
  - %c : 로깅이 발생한 카테고리
  - %C : 로깅이 발생한 클래스 명
  - %m : 로그 메세지
  - %n : 줄바꿈
  - %% : %출력
  - %r : 어플리케이션 실행 후 로깅이 발생한 시점까지의 기간
- root : 설정한 Appender를 참조하여 로그의 레벨을 설정할 수 있다.
  - root는 전역설정, logger는 지역설정
- 
```xml
<!-- RollingFileAppender 설정: 로그 메시지를 파일에 기록하고, 파일을 일정 시간마다 롤링 -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 로그 파일의 이름과 위치 지정 -->
        <file>${LOG_FILE}</file>
        <!-- 시간 기반 롤링 정책 설정 -->
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- 하루가 지나면 새로운 로그 파일이 생성되고 이전 로그 파일은 압축되어 저장 -->
            <fileNamePattern>${LOG_FILE}.%d{yyyy-MM-dd}.gz</fileNamePattern>
            <!-- 최대 5일치 로그 파일 보관 -->
            <maxHistory>5</maxHistory>
        </rollingPolicy>
        <!-- 로그 메시지의 출력 형식 지정 -->
        <encoder>
            <pattern>[%d{yyyy-MM-dd HH:mm:ss}] [%-5level] %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
```