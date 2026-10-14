package cn.openatom.events;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="events", indexes={@Index(name="idx_event_time",columnList="event_time"),@Index(name="idx_organizer",columnList="organizer_id")})
public class Event {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=120) private String title;
    @Column(nullable=false,length=5000) private String description;
    @Column(nullable=false,length=200) private String location;
    @Column(name="event_time",nullable=false) private Instant eventTime;
    @Column(nullable=false) private int capacity;
    @Column(name="registered_count",nullable=false) private int registeredCount;
    @Column(nullable=false) private boolean cancelled;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="organizer_id",nullable=false) private User organizer;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    protected Event() {}
    public Event(EventService.EventInput input, User organizer) {
        this.organizer=organizer; this.createdAt=Instant.now(); update(input);
    }
    public void update(EventService.EventInput input) {
        this.title=input.title().strip(); this.description=input.description().strip();
        this.location=input.location().strip(); this.eventTime=input.eventTime().toInstant();
        this.capacity=input.capacity(); this.updatedAt=Instant.now();
    }
    public void cancel() { this.cancelled=true; this.updatedAt=Instant.now(); }
    public void addRegistration() { this.registeredCount++; this.updatedAt=Instant.now(); }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public Instant getEventTime() { return eventTime; }
    public int getCapacity() { return capacity; }
    public int getRegisteredCount() { return registeredCount; }
    public boolean isCancelled() { return cancelled; }
    public User getOrganizer() { return organizer; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
