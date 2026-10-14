# 第11题：线上活动报名系统

题目要求、完整解题过程与面试追问见 [总说明](../../总说明.md)。本题与第10题共用一份 [Web源码](../../shared/web/)，截图与记录链接到仓库evidence。


## 源码目录与文件

[打开源码目录](../../shared/web)；下面的链接直接打开源文件，不是截图。

- [ApiResponse.java](../../shared/web/src/main/java/cn/openatom/events/ApiResponse.java)
- [AuthController.java](../../shared/web/src/main/java/cn/openatom/events/AuthController.java)
- [AuthService.java](../../shared/web/src/main/java/cn/openatom/events/AuthService.java)
- [BusinessException.java](../../shared/web/src/main/java/cn/openatom/events/BusinessException.java)
- [CacheConfig.java](../../shared/web/src/main/java/cn/openatom/events/CacheConfig.java)
- [Event.java](../../shared/web/src/main/java/cn/openatom/events/Event.java)
- [EventApplication.java](../../shared/web/src/main/java/cn/openatom/events/EventApplication.java)
- [EventCacheKey.java](../../shared/web/src/main/java/cn/openatom/events/EventCacheKey.java)
- [EventController.java](../../shared/web/src/main/java/cn/openatom/events/EventController.java)
- [EventListsChanged.java](../../shared/web/src/main/java/cn/openatom/events/EventListsChanged.java)
- [EventNotFoundException.java](../../shared/web/src/main/java/cn/openatom/events/EventNotFoundException.java)
- [EventRepository.java](../../shared/web/src/main/java/cn/openatom/events/EventRepository.java)
- [EventService.java](../../shared/web/src/main/java/cn/openatom/events/EventService.java)
- [GlobalExceptionHandler.java](../../shared/web/src/main/java/cn/openatom/events/GlobalExceptionHandler.java)
- [Registration.java](../../shared/web/src/main/java/cn/openatom/events/Registration.java)
- [RegistrationRepository.java](../../shared/web/src/main/java/cn/openatom/events/RegistrationRepository.java)
- [SecurityConfig.java](../../shared/web/src/main/java/cn/openatom/events/SecurityConfig.java)
- [UnauthorizedAccessException.java](../../shared/web/src/main/java/cn/openatom/events/UnauthorizedAccessException.java)
- [User.java](../../shared/web/src/main/java/cn/openatom/events/User.java)
- [UserRepository.java](../../shared/web/src/main/java/cn/openatom/events/UserRepository.java)
- [BrowserWorkflowTest.java](../../shared/web/src/test/java/cn/openatom/events/BrowserWorkflowTest.java)
- [EventPlatformTest.java](../../shared/web/src/test/java/cn/openatom/events/EventPlatformTest.java)

## 答题过程与代码讲解

**第1步：阶段1：先建模，再接通CRUD。** User发起Event，Registration关联User和Event。Controller收参/身份，Service做业务与事务，Repository访问库；ApiResponse统一输出，DTO避免直接暴露数据库实体。

**第2步：阶段2：后端核验主人。** 游客可GET列表/详情；写操作先验证Token。修改/取消再比较organizer.id与当前用户ID，不能凭前端按钮是否显示决定权限。事务提交后清eventLists专用缓存，因为活动变更会影响多个页和筛选。

**第3步：阶段3：用数据库保护最后一个名额。** register先调用findLocked获得活动行锁，随后检查时间/取消/重复/容量，写Registration并增加人数，整体事务提交。另一请求等待锁后重新读取容量，满了返回409。唯一索引兜底重复报名，但本身不防不同人超卖。

**第4步：阶段4：页面请求真实接口。** 原生页面api()统一拼/api路径、Bearer请求头和JSON，再fetch到后端。Dockerfile构建阶段产jar，运行阶段用JRE启动；Compose负责MySQL、Redis及连接配置，尚需实际容器证据。

**第5步：沿着一次报名请求读代码。** 按钮→fetch POST /api/events/id/registrations→Security认证→EventController取uid→EventService事务报名→EventRepository行锁与RegistrationRepository写入→提交后清缓存→ApiResponse返回→页面刷新人数。

## 关键源码与逐步解读

为便于阅读，这里展示关键实现；上方目录链接包含全部实体、接口、配置、测试和页面。
### 对照实际源码

以下展示当前仓库的真实代码。完整文件可直接点击文件名；类内片段依赖所属类的字段、imports及其他方法，不能单独编译。

#### [EventRepository.java](../../shared/web/src/main/java/cn/openatom/events/EventRepository.java)（完整文件）

```java
package cn.openatom.events;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event,Long>, JpaSpecificationExecutor<Event> {
    // Serialize modifications and registrations on the same activity across app instances.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.id=:id")
    Optional<Event> findLocked(@Param("id") Long id);
}
```

#### [Registration.java](../../shared/web/src/main/java/cn/openatom/events/Registration.java)（完整文件）

```java
package cn.openatom.events;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="registrations",uniqueConstraints=@UniqueConstraint(name="uk_event_user",columnNames={"event_id","user_id"}))
public class Registration {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="event_id",nullable=false) private Event event;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
    @Column(name="registered_at",nullable=false) private Instant registeredAt;
    protected Registration() {}
    public Registration(Event event, User user) { this.event=event; this.user=user; this.registeredAt=Instant.now(); }
    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public User getUser() { return user; }
    public Instant getRegisteredAt() { return registeredAt; }
}
```

#### [ApiResponse.java](../../shared/web/src/main/java/cn/openatom/events/ApiResponse.java)（完整文件）

```java
package cn.openatom.events;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS)
public record ApiResponse<T>(int code, String message, T data) {
    public static <T> ApiResponse<T> ok(String message, T data) { return new ApiResponse<>(0, message, data); }
    public static ApiResponse<Void> error(int code, String message) { return new ApiResponse<>(code, message, null); }
}
```

#### [EventService.java](../../shared/web/src/main/java/cn/openatom/events/EventService.java)（类内片段）

```java
    @Transactional
    public RegistrationView register(Long id,Long userId) {
        Event event=locked(id);
        if(event.isCancelled()) throw new BusinessException(409,"活动已取消");
        if(!event.getEventTime().isAfter(Instant.now())) throw new BusinessException(409,"活动已开始或结束");
        if(registrations.existsByEventIdAndUserId(id,userId)) throw new BusinessException(409,"你已报名该活动");
        if(event.getRegisteredCount()>=event.getCapacity()) throw new BusinessException(409,"活动名额已满");
        Registration record=registrations.saveAndFlush(new Registration(event,user(userId)));
        event.addRegistration(); events.flush(); changed();
        return new RegistrationView(record.getId(),id,userId,record.getRegisteredAt());
    }
```

#### [EventService.java](../../shared/web/src/main/java/cn/openatom/events/EventService.java)（类内片段）

```java
    @Transactional(readOnly=true)
    @Cacheable(value="eventLists", key="T(cn.openatom.events.EventCacheKey).of(#page,#size,#keyword,#status)", condition="#page >= 0 && #size > 0 && #size <= 100")
    public EventPage list(int page,int size,String keyword,String status) {
        if(page<0 || size<1 || size>100) throw new BusinessException(400,"page须大于等于0，size须为1-100");
        if(keyword.length()>120) throw new BusinessException(400,"关键词不能超过120字");
        if(!status.isEmpty() && !status.equals("UPCOMING") && !status.equals("ENDED"))
            throw new BusinessException(400,"status仅支持UPCOMING或ENDED");
        Instant now=Instant.now();
        var result=events.findAll((root,query,cb)->{
            List<Predicate> conditions=new ArrayList<>();
            conditions.add(cb.isFalse(root.get("cancelled")));
            if(!keyword.isBlank()) {
                // Treat percent/underscore as literal text rather than user-supplied wildcards.
                String term="%"+keyword.toLowerCase(Locale.ROOT).replace("\\","\\\\").replace("%","\\%").replace("_","\\_")+"%";
                conditions.add(cb.or(cb.like(cb.lower(root.get("title")),term,'\\'),cb.like(cb.lower(root.get("description")),term,'\\')));
            }
            if(status.equals("UPCOMING")) conditions.add(cb.greaterThan(root.get("eventTime"),now));
            if(status.equals("ENDED")) conditions.add(cb.lessThanOrEqualTo(root.get("eventTime"),now));
            return cb.and(conditions.toArray(Predicate[]::new));
        },PageRequest.of(page,size,Sort.by("eventTime").ascending().and(Sort.by("id"))));
        return new EventPage(result.getContent().stream().map(EventView::from).toList(),result.getTotalElements(),result.getTotalPages(),page,size);
    }
```

#### [CacheConfig.java](../../shared/web/src/main/java/cn/openatom/events/CacheConfig.java)（完整文件）

```java
package cn.openatom.events;

import java.time.Duration;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.*;
import org.springframework.transaction.event.*;

@Configuration
public class CacheConfig {
    @Bean @Profile("mysql")
    RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(30)).disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new JdkSerializationRedisSerializer()));
    }
    // Clear only event-list cache, after commit. All list pages/filters can shift after a mutation.
    @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
    @CacheEvict(value="eventLists",allEntries=true)
    public void changed(EventListsChanged change) {}
}
```

#### [EventCacheKey.java](../../shared/web/src/main/java/cn/openatom/events/EventCacheKey.java)（完整文件）

```java
package cn.openatom.events;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
public final class EventCacheKey {
    private EventCacheKey() {}
    public static String of(int page,int size,String keyword,String status) {
        // All dimensions + a 30-second time bucket: status changes with the clock even without writes.
        return page+":"+size+":"+Base64.getUrlEncoder().encodeToString(keyword.getBytes(StandardCharsets.UTF_8))
                +":"+status+":"+(java.time.Instant.now().getEpochSecond()/30);
    }
}
```

#### [页面统一请求函数](../../shared/web/src/main/resources/static/index.html)（页面内片段）

```javascript
async function api(path,options={}){const headers={'Content-Type':'application/json',...(token?{Authorization:'Bearer '+token}:{}),...options.headers};let response;try{response=await fetch('/api'+path,{...options,headers})}catch(e){throw Error('网络连接失败，请检查网络或确认服务已启动。')}let result;try{result=await response.json()}catch(e){throw Error('服务器返回了无法识别的响应。')}if(!response.ok||result.code!==0){if(response.status===401){token=null;user=null;localStorage.removeItem('openatom.token');updateAccount()}throw Error(result.message||'请求失败，请稍后重试。')}return result.data}
```

这个函数依赖页面中已有的token、user与updateAccount。它负责发真实fetch请求，不是模拟响应。

#### [Dockerfile](../../shared/web/Dockerfile)（完整文件）

```dockerfile
FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp clean package

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /build/target/event-platform-1.0.0.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

## 运行

JDK17+Maven。在仓库根目录运行同一份项目：

```sh
mvn -f shared/web/pom.xml verify
mvn -f shared/web/pom.xml spring-boot:run
```

浏览器访问 http://localhost:8080 。默认H2+Caffeine。完整MySQL+Redis配置见 [Web运行说明](../../shared/web/README.md)；Compose在仓库根目录执行。第10题重点是认证，第11题重点是活动与报名；两题共用代码，不重复存放。

## 证据来源与完成状态

旧Web截图来自真实Spring MockMvc浏览器流程，不是Docker截图；六组集成测试和14项真实HTTP检查通过，但测试使用H2+Caffeine。Web MySQL+Redis、Docker实际运行证据待补。

![已保存运行证据，来源如上](../../evidence/screenshots/web/01-guest.png)

![已保存运行证据，来源如上](../../evidence/screenshots/web/02-login.png)

![已保存运行证据，来源如上](../../evidence/screenshots/web/03-created.png)

![已保存运行证据，来源如上](../../evidence/screenshots/web/04-registered.png)

![已保存运行证据，来源如上](../../evidence/screenshots/web/05-cancelled.png)

![已保存运行证据，来源如上](../../evidence/screenshots/web/06-mobile.png)

