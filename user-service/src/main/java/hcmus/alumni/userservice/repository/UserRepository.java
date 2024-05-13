package hcmus.alumni.userservice.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.userservice.dto.ISearchDto;
import hcmus.alumni.userservice.model.FacultyModel;
import hcmus.alumni.userservice.model.UserModel;

public interface UserRepository extends JpaRepository<UserModel, String> {
	Optional<UserModel> findById(String id);
	
	UserModel findByEmailAndPass(String email, String pass);

	UserModel findByEmail(String email);

	@Query("SELECT u FROM UserModel u WHERE u.id = :userId")
	UserModel findUserById(@Param("userId") String userId);

	@Query("SELECT u.avatarUrl FROM UserModel u WHERE u.id = :userId")
	String getAvatarUrl(@Param("userId") String userId);

	@Transactional
	@Modifying
	@Query("UPDATE UserModel u SET u.avatarUrl = :avatarUrl WHERE u.id = :userId")
	int setAvatarUrl(@Param("userId") String userId, @Param("avatarUrl") String avatarUrl);

	@Transactional
	@Modifying
	@Query("UPDATE UserModel u SET u.fullName = :fullName, u.socialMediaLink = :socialMediaLink, u.faculty = :faculty WHERE u.id = :userId")
	int setDataFirstVerifyAlumni(@Param("userId") String userId, @Param("fullName") String fullName,
			@Param("socialMediaLink") String socialMediaLink, @Param("faculty") FacultyModel faculty);

	@Query("SELECT u.fullName FROM UserModel u WHERE u.id = :userId")
	String findFullNameByUserId(@Param("userId") String userId);

	@Query("SELECT u.avatarUrl FROM UserModel u WHERE u.id = :userId")
	String findAvatarUrlByUserId(@Param("userId") String userId);
	
	@Query("SELECT DISTINCT u " + 
		   "FROM UserModel u " + 
		   "LEFT JOIN u.status s " + 
		   "LEFT JOIN u.faculty f " + 
		   "LEFT JOIN u.alumni a " + 
		   "WHERE u.fullName LIKE %:fullName% " +
		   "AND (:facultyId IS NULL OR f.id = :facultyId) " + 
		   "AND (:statusId IS NULL OR s.id = :statusId) " +
		   "AND (:beginningYear IS NULL OR a.beginningYear = :beginningYear OR :beginningYear = 0) " +
		   "AND s.id != 3")
	Page<ISearchDto> searchUsers(String fullName, Integer statusId, Integer facultyId, Integer beginningYear, Pageable pageable);


	@Query("SELECT COUNT(n) FROM UserModel n JOIN n.status s WHERE s.id != 4")
	Long getCountByNotDelete();

	@Query("SELECT COUNT(n) FROM UserModel n JOIN n.status s WHERE s.id = :statusId")
	Long getCountByStatusId(@Param("statusId") Integer statusId);
}