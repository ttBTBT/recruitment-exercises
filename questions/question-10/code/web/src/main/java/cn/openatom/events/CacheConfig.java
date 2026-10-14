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
