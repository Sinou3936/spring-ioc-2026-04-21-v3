# Spring IoC Container 직접 구현

Spring Framework의 IoC(Inversion of Control) 컨테이너를 직접 구현하는 프로젝트입니다.

## 구현 내용

### 애노테이션

| 애노테이션 | 역할 |
|---|---|
| `@Component` | 일반 컴포넌트 빈 등록 |
| `@Service` | 서비스 레이어 빈 등록 |
| `@Repository` | 리포지토리 레이어 빈 등록 |
| `@Configuration` | 설정 클래스 등록 |
| `@Bean` | `@Configuration` 클래스 내 메서드로 빈 등록 |

### ApplicationContext

`Reflections` 라이브러리를 사용해 지정한 베이스 패키지를 스캔하고, 애노테이션이 붙은 클래스를 자동으로 빈으로 등록합니다.

**주요 기능:**
- 패키지 스캔으로 빈 자동 등록 (`init()`)
- 이름 기반 빈 조회 (`genBean(beanName)`)
- **싱글톤 보장** — 한 번 생성된 빈은 내부 Map에 캐싱되어 재사용
- **생성자 주입** — Lombok `@RequiredArgsConstructor`와 연동하여 파라미터 타입으로 의존 빈을 자동 주입
- **`@Configuration` + `@Bean` 지원** — 메서드 반환값을 빈으로 등록, 메서드 파라미터도 자동 주입

## 테스트 케이스 (t1 ~ t8)

| 테스트 | 설명 |
|---|---|
| t1 | `ApplicationContext` 객체 생성 확인 |
| t2 | `@Service` 빈(`testPostService`) 조회 |
| t3 | 동일 빈을 두 번 조회했을 때 같은 인스턴스인지 확인 (싱글톤) |
| t4 | `@Repository` 빈(`testPostRepository`) 조회 |
| t5 | `testPostService`가 `testPostRepository`를 주입받았는지 확인 |
| t6 | `testFacadePostService`가 `testPostService`, `testPostRepository` 둘 다 주입받았는지 확인 |
| t7 | `@Bean` 메서드로 등록한 `JavaTimeModule` 빈 조회 |
| t8 | `@Bean` 메서드의 파라미터로 t7의 빈을 주입받아 `ObjectMapper` 빈 생성 |

## 기술 스택

- Java 21
- Gradle (Kotlin DSL)
- Reflections (패키지 스캔)
- Lombok
- JUnit 5 / AssertJ