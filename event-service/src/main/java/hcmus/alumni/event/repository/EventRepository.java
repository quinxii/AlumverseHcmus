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
	@Query(value = "select distinct p.name from role_permission rp " +
		"join role r on r.id = rp.role_id and r.is_delete = false " +
		"join permission p on p.id = rp.permission_id and p.is_delete = false " +
		"where r.name in :role and p.name like :domain% and rp.is_delete = false", nativeQuery = true)
	List<String> getPermissions(List<String> role, String domain);
	
	@Query("SELECT e " +
        "FROM EventModel e " +
        "LEFT JOIN e.status s " +
        "LEFT JOIN e.faculty f " +
        "LEFT JOIN e.tags t " +
        "WHERE (:statusId IS NULL OR s.id = :statusId) " +
        "AND (:facultyId IS NULL OR f.id = :facultyId) " +
        "AND e.title LIKE %:title% " +
        "AND (CASE " +
        "       WHEN :mode = 1 THEN e.organizationTime >= :startDate " +
        "       WHEN :mode = 2 THEN e.organizationTime < :startDate " +
        "       ELSE true " +
        "   END) " +
        "AND (:tagsId IS NULL OR t.id IN :tagsId)")
	Page<IEventDto> searchEvents(
			@Param("title") String title,
			@Param("statusId") Integer statusId,
			@Param("facultyId") Integer facultyId,
			@Param("tagsId") List<Integer> tagsId,
			@Param("startDate") Date startDate,
			@Param("mode") Integer mode,
			Pageable pageable);
	
	@Query("SELECT e " +
			"FROM EventModel e " +
			"WHERE e.id = :id")
	Optional<IEventDto> findEventById(String id);

    @Transactional
    @Modifying
    @Query("UPDATE EventModel e SET e.views = e.views + 1 WHERE e.id = :id")
    int incrementEventViews(String id);

    @Query("SELECT e " + 
		"FROM EventModel e " + 
    	"JOIN e.status s " + 
		"WHERE s.id = 2 AND e.organizationTime >= :startDate")
    Page<IEventDto> getHotEvents(
    		Date startDate,
		    Pageable pageable);
    
	@Query("SELECT e " + 
		"FROM EventModel e " + 
		"LEFT JOIN ParticipantEventModel p " + 
		"ON p.id.eventId = e.id " + 
		"WHERE p.id.userId = :userId " + 
		"AND (CASE " +
		"       WHEN :mode = 1 THEN e.organizationTime >= :startDate " +
		"       WHEN :mode = 2 THEN e.organizationTime < :startDate " +
		"       ELSE true " +
		"   END)")
	Page<IEventDto> getUserParticipatedEvents(
			@Param("userId") String userId, 
			@Param("startDate") Date startDate,
			@Param("mode") Integer mode,
			Pageable pageable);
    
    @Transactional
	@Modifying
	@Query("UPDATE EventModel n SET n.participants = n.participants + :count WHERE n.id = :id")
	int participantCountIncrement(String id,@Param("count") Integer count);
    
    @Transactional
	@Modifying
	@Query("UPDATE EventModel n SET n.childrenCommentNumber = n.childrenCommentNumber + :count WHERE n.id = :id")
	int commentCountIncrement(String id,@Param("count") Integer count);
}
