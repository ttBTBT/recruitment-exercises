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
