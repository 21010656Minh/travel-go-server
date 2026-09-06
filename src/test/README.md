# 🧪 Hướng dẫn Unit Test cho Spring Boot - Travel Social Network

## 📋 Tổng quan

Project này sử dụng **Spring Boot 3.5.4** với **Java 21** và có đầy đủ test suite bao gồm:
- **Unit Tests**: Test logic business riêng lẻ
- **Integration Tests**: Test tương tác giữa các components
- **Repository Tests**: Test JPA queries với H2 database
- **Controller Tests**: Test HTTP endpoints

## 🏗️ Cấu trúc Test

```
src/test/java/
├── config/                           # Test configuration
│   ├── TestConfig.java              # Mock beans cho testing
│   └── TestSecurityConfig.java      # Disable security cho testing
├── integration/                      # Integration tests
│   └── AuthIntegrationTest.java     # Test complete flow
├── repositories/                     # Repository tests
│   └── UserRepositoryTest.java      # Test JPA queries
├── services/                         # Service unit tests
│   └── auth/
│       └── AuthServiceTest.java     # Test business logic
└── controllers/                      # Controller tests
    └── AuthControllerTest.java      # Test HTTP endpoints

src/test/resources/
└── application-test.properties      # Test configuration
```

## 🎯 Các loại Test trong Project

### 1. **Unit Test** - AuthServiceTest.java
```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenProvider jwtGenerator;
    @Mock private IMailService mailService;
    @InjectMocks private AuthService authService;
    
    @Test
    void registerService_ValidData_ShouldReturnUser() {
        // Test logic business thuần túy
    }
}
```

**Đặc điểm:**
- ✅ Nhanh nhất (không load Spring Context)
- ✅ Mock tất cả dependencies
- ✅ Test logic business riêng lẻ
- ✅ Dễ debug và maintain

### 2. **Controller Test** - AuthControllerTest.java
```java
@WebMvcTest(controllers = AuthController.class)
@Import({TestSecurityConfig.class, TestConfig.class})
class AuthControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private IAuthService authService;
    
    @Test
    void register_ValidData_ShouldReturnCreated() throws Exception {
        mockMvc.perform(post("/api/v2/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated());
    }
}
```

**Đặc điểm:**
- ✅ Test HTTP endpoints thực tế
- ✅ Verify JSON response structure
- ✅ Test security configuration
- ✅ Mock service layer

### 3. **Repository Test** - UserRepositoryTest.java
```java
@DataJpaTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class UserRepositoryTest {
    @Autowired private TestEntityManager entityManager;
    @Autowired private UserRepository userRepository;
    
    @Test
    void findByEmail_ExistingEmail_ShouldReturnUser() {
        entityManager.persistAndFlush(testUser);
        Optional<User> found = userRepository.findByEmail("test@example.com");
        assertThat(found).isPresent();
    }
}
```

**Đặc điểm:**
- ✅ Test JPA queries với database thực
- ✅ Sử dụng H2 in-memory database
- ✅ Test entity relationships
- ✅ Verify database constraints

### 4. **Integration Test** - AuthIntegrationTest.java
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestConfig.class, TestSecurityConfig.class})
@Transactional
class AuthIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    
    @Test
    void registerAndLogin_CompleteFlow_ShouldWork() throws Exception {
        // Test complete user flow: register → login
    }
}
```

**Đặc điểm:**
- ✅ Test complete user flows
- ✅ Load full Spring context
- ✅ Test tương tác giữa các layers
- ✅ Verify end-to-end functionality

## ⚙️ Test Configuration

### 1. **application-test.properties**
```properties
# Test configuration
spring.application.name=travel_social_network_server

# Database configuration for testing (H2)
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA configuration for testing
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# API configuration
api.base-url=/api/v2
api.client.url=http://localhost:3000

# JWT configuration for testing
jwt.secret.key=test-secret-key-for-testing-only
jwt.expiration-seconds=3600

# Disable external services for testing
spring.cache.type=none
spring.flyway.enabled=false
```

### 2. **TestConfig.java** - Mock Beans
```java
@TestConfiguration
public class TestConfig {
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public TokenProvider testTokenProvider() {
        return new TokenProvider() {
            @Override
            public String generateToken(User user, ProviderTypeEnum provider) {
                return "test-jwt-token";
            }
            @Override
            public boolean validateToken(String token) {
                return true;
            }
            @Override
            public String extractEmail(String token) throws SignatureVerificationException {
                return "test@example.com";
            }
        };
    }
}
```

### 3. **TestSecurityConfig.java** - Disable Security
```java
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {
    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v2/auth/**").permitAll()
                        .anyRequest().permitAll() // Allow all for testing
                )
                .build();
    }
}
```

## 🎨 Best Practices

### 1. **Naming Convention**
```java
// Format: methodName_scenario_expectedResult
@Test
void registerService_ValidData_ShouldReturnUser() { }
@Test
void loginService_InvalidPassword_ShouldThrowException() { }
@Test
void findByEmail_NonExistingEmail_ShouldReturnEmpty() { }
```

### 2. **Test Structure (AAA Pattern)**
```java
@Test
void testMethod() {
    // Arrange (Given) - Setup test data
    when(mock.method()).thenReturn(value);
    User testUser = createTestUser();
    
    // Act (When) - Execute method under test
    var result = service.method(input);
    
    // Assert (Then) - Verify results
    assertThat(result).isNotNull();
    verify(mock).method();
}
```

### 3. **Mocking Guidelines**
```java
// ✅ Mock external dependencies
@Mock private UserRepository userRepository;
@Mock private IMailService mailService;

// ✅ Mock Spring components
@MockBean private IAuthService authService;

// ✅ Mock CompletableFuture return types
when(mailService.sendMail(any())).thenReturn(CompletableFuture.completedFuture(null));

// ❌ Don't mock the class under test
// @Mock private AuthService authService; // WRONG!
```

### 4. **Assertions**
```java
// ✅ AssertJ (Recommended)
assertThat(result).isNotNull();
assertThat(result.getEmail()).isEqualTo("test@example.com");
assertThat(result.getUserId()).isInstanceOf(UUID.class);
assertThatThrownBy(() -> service.method())
    .isInstanceOf(ResourceNotFoundException.class);

// ✅ JUnit 5
assertEquals("expected", actual);
assertThrows(Exception.class, () -> service.method());
```

## 🚀 Chạy Tests

### **Chạy từng loại test:**
```bash
# Unit tests (nhanh nhất)
.\mvnw.cmd test -Dtest=AuthServiceTest

# Controller tests
.\mvnw.cmd test -Dtest=AuthControllerTest

# Repository tests
.\mvnw.cmd test -Dtest=UserRepositoryTest

# Integration tests
.\mvnw.cmd test -Dtest=AuthIntegrationTest

# Chạy tất cả tests
.\mvnw.cmd test
```

### **Chạy với pattern:**
```bash
# Tất cả auth tests
.\mvnw.cmd test -Dtest="*Auth*Test"

# Tất cả service tests
.\mvnw.cmd test -Dtest="*Service*Test"

# Tất cả repository tests
.\mvnw.cmd test -Dtest="*Repository*Test"
```

### **Chạy với coverage:**
```bash
.\mvnw.cmd test jacoco:report
```

## 📊 Test Coverage

### **Mục tiêu:**
- 🎯 **80%+ code coverage** cho business logic
- 🎯 **100% coverage** cho critical paths
- 🎯 Test cả **happy path** và **error scenarios**

### **Focus areas:**
- ✅ Business logic trong services
- ✅ HTTP endpoints trong controllers
- ✅ Database queries trong repositories
- ✅ Error handling và edge cases
- ❌ Getters/setters (không cần test)
- ❌ Configuration classes (test integration)

## 🔧 Troubleshooting

### **Lỗi thường gặp:**

1. **403 Forbidden trong Controller Test**
   ```java
   // ✅ Solution: Import TestSecurityConfig
   @Import(TestSecurityConfig.class)
   ```

2. **Table not found trong Repository Test**
   ```properties
   # ✅ Solution: Sử dụng hibernate.ddl-auto=create-drop
   spring.jpa.hibernate.ddl-auto=create-drop
   ```

3. **CompletableFuture mock lỗi**
   ```java
   // ✅ Solution: Mock đúng return type
   when(mailService.sendMail(any())).thenReturn(CompletableFuture.completedFuture(null));
   ```

4. **UUID vs Long type mismatch**
   ```java
   // ✅ Solution: Sử dụng UUID trong test data
   .userId(UUID.randomUUID())
   ```

## 📈 Kết quả Test hiện tại

| Test Class | Loại | Trạng thái | Mô tả |
|------------|------|------------|-------|
| **AuthServiceTest** | Unit Test | ✅ PASS | Test business logic với mocks |
| **AuthControllerTest** | Controller Test | ✅ PASS | Test HTTP endpoints |
| **UserRepositoryTest** | Repository Test | ✅ PASS | Test JPA queries với H2 |
| **AuthIntegrationTest** | Integration Test | ✅ PASS | Test complete flows |

## 🎉 Kết luận

Test suite này cung cấp coverage toàn diện cho Travel Social Network API:
- **Unit tests** đảm bảo logic business đúng
- **Integration tests** đảm bảo các components hoạt động cùng nhau
- **Repository tests** đảm bảo database operations đúng
- **Controller tests** đảm bảo API endpoints hoạt động đúng

Tất cả tests đều sử dụng best practices và có thể chạy độc lập hoặc cùng nhau.
