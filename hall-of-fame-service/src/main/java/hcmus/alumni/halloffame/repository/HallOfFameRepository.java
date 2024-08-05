package hcmus.alumni.halloffame.repository;

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

import hcmus.alumni.halloffame.dto.IHallOfFameDto;
import hcmus.alumni.halloffame.dto.IHallOfFameListDto;
import hcmus.alumni.halloffame.model.HallOfFameModel;

public interface HallOfFameRepository extends JpaRepository<HallOfFameModel, String> {
	Optional<IHallOfFameDto> findHallOfFameById(String id);

	Optional<HallOfFameModel> findById(String id);

	@Query("SELECT h " +
			"FROM HallOfFameModel h " +
			"LEFT JOIN h.status s " +
			"LEFT JOIN h.faculty f " +
			"WHERE (:statusId IS NULL OR s.id = :statusId) " +
			"AND (:facultyId IS NULL OR f.id = :facultyId) " +
			"AND (:beginningYear IS NULL OR h.beginningYear = :beginningYear) " +
			"AND s.id != 4 " +
			"AND (:title IS NULL OR h.title LIKE %:title%)")
	Page<IHallOfFameListDto> searchHof(String title, Integer statusId,
			Integer facultyId, Integer beginningYear, Pageable pageable);

	@Query("SELECT h " +
			"FROM HallOfFameModel h " +
			"LEFT JOIN h.status s " +
			"LEFT JOIN h.faculty f " +
			"WHERE (:statusId IS NULL OR s.id = :statusId) " +
			"AND (f.id = (SELECT u.facultyId FROM UserModel u WHERE u.id = :userId) OR f.id IS NULL) " +
			"AND (:beginningYear IS NULL OR h.beginningYear = :beginningYear) " +
			"AND s.id != 4 " +
			"AND (:title IS NULL OR h.title LIKE %:title%)")
	Page<IHallOfFameListDto> searchHofByUserFaculty(String userId, String title, Integer statusId,
			Integer beginningYear, Pageable pageable);

	@Query("SELECT COUNT(n) FROM HallOfFameModel n JOIN n.status s WHERE s.name = :statusName")
	Long getCountByStatus(@Param("statusName") String statusName);

	@Query("SELECT COUNT(n) FROM HallOfFameModel n JOIN n.status s WHERE s.id != 4")
	Long getCountByNotDelete();

	@Transactional
	@Modifying
	@Query("UPDATE HallOfFameModel n SET n.views = n.views + 1 WHERE n.id = :id")
	int viewsIncrement(String id);

	@Query("SELECT n from HallOfFameModel n JOIN n.status s WHERE s.name = \"Chờ\" AND n.publishedAt <= :now")
	List<HallOfFameModel> getScheduledHof(Date now);

	@Query(value = "select distinct p.name from role_permission rp " +
			"join permission p on p.id = rp.permission_id and p.is_delete = false " +
			"join role r on r.id = rp.role_id and r.is_delete = false " +
			"where r.id in (select role_id from user_role where user_id = :userId) and p.name like :domain% and rp.is_delete = false;", nativeQuery = true)
	List<String> getPermissions(String userId, String domain);

	@Query("SELECT hof FROM HallOfFameModel hof WHERE hof.status.id = 2 ORDER BY function('RAND') LIMIT :number")
	List<IHallOfFameListDto> findRandomHofEntries(@Param("number") Integer number);
}