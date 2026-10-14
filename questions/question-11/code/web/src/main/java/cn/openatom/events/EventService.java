package cn.openatom.events;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.*;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

@Service
public class EventService {
    public record EventInput(@NotBlank @Size(max=120) String title,
                             @NotBlank @Size(max=5000) String description,
                             @NotBlank @Size(max=200) String location,
                             @NotNull @Future OffsetDateTime eventTime,
                             @Min(1) @Max(100000) int capacity) {}
    public record EventView(Long id,String title,String description,String location,Instant eventTime,
                            int capacity,int registeredCount,Long organizerId,String organizerName,
                            boolean cancelled,Instant createdAt,Instant updatedAt) implements Serializable {
        static EventView from(Event e) {
            return new EventView(e.getId(),e.getTitle(),e.getDescription(),e.getLocation(),e.getEventTime(),
                    e.getCapacity(),e.getRegisteredCount(),e.getOrganizer().getId(),e.getOrganizer().getUsername(),
                    e.isCancelled(),e.getCreatedAt(),e.getUpdatedAt());
        }
    }
    public record EventPage(List<EventView> content,long totalElements,int totalPages,int page,int size) implements Serializable {}
    public record RegistrationView(Long id,Long eventId,Long userId,Instant registeredAt) {}
    public record RegistrationStatus(boolean registered) {}
    private final EventRepository events;
    private final UserRepository users;
    private final RegistrationRepository registrations;
    private final org.springframework.context.ApplicationEventPublisher publisher;
    public EventService(EventRepository events, UserRepository users, RegistrationRepository registrations,
                        org.springframework.context.ApplicationEventPublisher publisher) {
        this.events=events; this.users=users; this.registrations=registrations; this.publisher=publisher;
    }
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
    @Transactional(readOnly=true)
    public EventView detail(Long id) { return EventView.from(events.findById(id).orElseThrow(EventNotFoundException::new)); }
    @Transactional
    public EventView create(Long userId,EventInput input) {
        Event event=events.saveAndFlush(new Event(input,user(userId))); changed(); return EventView.from(event);
    }
    @Transactional
    public EventView update(Long id,Long userId,EventInput input) {
        Event event=locked(id); checkOwner(event,userId);
        if(event.isCancelled()) throw new BusinessException(409,"已取消的活动不可修改");
        if(!event.getEventTime().isAfter(Instant.now())) throw new BusinessException(409,"已开始的活动不可修改");
        if(input.capacity()<event.getRegisteredCount()) throw new BusinessException(409,"名额不能小于已报名人数");
        event.update(input); events.flush(); changed(); return EventView.from(event);
    }
    @Transactional
    public void cancel(Long id,Long userId) {
        Event event=locked(id); checkOwner(event,userId); event.cancel(); events.flush(); changed();
    }
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
    @Transactional(readOnly=true)
    public RegistrationStatus registrationStatus(Long id,Long userId) {
        if(!events.existsById(id)) throw new EventNotFoundException();
        return new RegistrationStatus(registrations.existsByEventIdAndUserId(id,userId));
    }
    private Event locked(Long id) { return events.findLocked(id).orElseThrow(EventNotFoundException::new); }
    private User user(Long id) { return users.findById(id).orElseThrow(()->new BusinessException(401,"用户已不存在")); }
    private void checkOwner(Event event,Long id) { if(!event.getOrganizer().getId().equals(id)) throw new UnauthorizedAccessException(); }
    private void changed() { publisher.publishEvent(new EventListsChanged()); }
}
