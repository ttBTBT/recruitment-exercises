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
