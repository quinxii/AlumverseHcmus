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
import hcmus.alumni.event.dto.IParticipantEventDto;
import hcmus.alumni.event.model.EventModel;

public interface EventRepository extends JpaRepository<EventModel, String> {        
	@Query("SELECT e " +
        "FROM EventModel e " +
        "LEFT JOIN e.status s " +
        "LEFT JOIN e.faculty f " +
        "WHERE (:statusId IS NULL OR s.id = :statusId) " +
        "AND (:facultyId IS NULL OR f.id = :facultyId) " +
        "AND e.title LIKE %:title%")
	Page<IEventDto> searchEvents(@Param("title") String title, @Param("statusId") Integer statusId, @Param("facultyId") Integer facultyId, Pageable pageable);

	@Query("SELECT e " +
		"FROM EventModel e " +
		"WHERE e.id = :id")
	Optional<IEventDto> findEventById(String id);

    @Transactional
    @Modifying
    @Query("UPDATE EventModel e SET e.views = e.views + 1 WHERE e.id = :id")
    int incrementEventViews(String id);

    @Query("SELECT n FROM EventModel n JOIN n.status s WHERE s.id = 2 AND n.publishedAt >= :startDate")
    Page<IEventDto> getHotEvents(Date startDate, Pageable pageable);
    
    @Query("SELECT pe.note as note, u.fullName AS fullName " +
		"FROM ParticipantEventModel pe " +
		"JOIN UserModel u ON pe.id.userId = u.id " +
		"WHERE pe.id.eventId = :id " +
		"AND pe.isDeleted = false")
     List<IParticipantEventDto> getParticipantsByEventId(@Param("id") String id);
}
