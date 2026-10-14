package cn.openatom.events;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService events;
    public EventController(EventService events) { this.events=events; }
    @GetMapping public ApiResponse<EventService.EventPage> list(@RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="6") int size,@RequestParam(defaultValue="") String keyword,
            @RequestParam(defaultValue="") String status) { return ApiResponse.ok("查询成功",events.list(page,size,keyword,status)); }
    @GetMapping("/{id}") public ApiResponse<EventService.EventView> detail(@PathVariable Long id) { return ApiResponse.ok("查询成功",events.detail(id)); }
    @PostMapping public ApiResponse<EventService.EventView> create(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody EventService.EventInput input) {
        return ApiResponse.ok("活动创建成功",events.create(uid(jwt),input));
    }
    @PutMapping("/{id}") public ApiResponse<EventService.EventView> update(@PathVariable Long id,@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody EventService.EventInput input) {
        return ApiResponse.ok("活动更新成功",events.update(id,uid(jwt),input));
    }
    @DeleteMapping("/{id}") public ApiResponse<Void> cancel(@PathVariable Long id,@AuthenticationPrincipal Jwt jwt) {
        events.cancel(id,uid(jwt)); return ApiResponse.ok("活动已取消",null);
    }
    @PostMapping("/{id}/registrations") public ApiResponse<EventService.RegistrationView> register(@PathVariable Long id,@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok("报名成功",events.register(id,uid(jwt)));
    }
    @GetMapping("/{id}/registrations/me") public ApiResponse<EventService.RegistrationStatus> registrationStatus(@PathVariable Long id,@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok("查询成功",events.registrationStatus(id,uid(jwt)));
    }
    private Long uid(Jwt jwt) { if(jwt==null) throw new BusinessException(401,"请先登录"); return Long.parseLong(jwt.getSubject()); }
}
