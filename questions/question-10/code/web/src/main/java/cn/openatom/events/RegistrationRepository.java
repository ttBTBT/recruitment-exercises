package cn.openatom.events;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RegistrationRepository extends JpaRepository<Registration,Long> {
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
    long countByEventId(Long eventId);
}
