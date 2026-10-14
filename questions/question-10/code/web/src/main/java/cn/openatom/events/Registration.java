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
