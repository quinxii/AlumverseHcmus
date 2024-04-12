package hcmus.alumni.event.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.event.dto.IEventDto;
import hcmus.alumni.event.model.EventModel;

public interface EventRepository  extends JpaRepository<EventModel, String> {
	Optional<EventModel> findById(String id);

    Optional<IEventDto> findEventById(String id);

    @Query("SELECT e FROM EventModel e JOIN e.status s WHERE s.id != 4 AND e.title like %:title%")
    Page<IEventDto> searchEvents(String title, Pageable pageable);

    @Query("SELECT e FROM EventModel e JOIN e.status s WHERE s.id = :statusId AND e.title like %:title%")
    Page<IEventDto> searchEventsByStatus(String title, Integer statusId, Pageable pageable);

    @Query("SELECT COUNT(e) FROM EventModel e JOIN e.status s WHERE s.name = :statusName")
    Long getCountByStatus(String statusName);

    @Query("SELECT COUNT(e) FROM EventModel e JOIN e.status s WHERE s.id != 4")
    Long getCountByNotDelete();

    @Query("SELECT e FROM EventModel e JOIN e.status s WHERE s.id = 1 AND e.publishedAt <= :now")
    List<EventModel> getScheduledEvents(Date now);

    @Transactional
    @Modifying
    @Query("UPDATE EventModel e SET e.views = e.views + 1 WHERE e.id = :id")
    int incrementEventViews(String id);
}
