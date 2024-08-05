package hcmus.alumni.userservice.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.userservice.dto.profile.IUserProfileDto;
import hcmus.alumni.userservice.dto.user.ISuggestionUserDto;
import hcmus.alumni.userservice.dto.user.IUserSearchDto;
import hcmus.alumni.userservice.model.FacultyModel;
import hcmus.alumni.userservice.model.UserModel;

public interface UserRepository extends JpaRepository<UserModel, String> {
	@Query("SELECT u FROM UserModel u WHERE u.id = :id")
	Optional<IUserSearchDto> findByIdCustom(String id);

	@Query("SELECT u FROM UserModel u WHERE u.id = :id AND u.statusId = 2")
	Optional<UserModel> findById(String id);

	UserModel findByEmailAndPass(String email, String pass);

	UserModel findByEmail(String email);

	UserModel findByEmailAndStatusId(String email, Integer statusId);

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

	@Transactional
	@Modifying
	@Query(value = "UPDATE user_role SET role_id = 4 WHERE user_id = :userId AND role_id = 5", nativeQuery = true)
	int updateRoleFromGuestToAlumni(String userId);

	@Query("SELECT u.fullName FROM UserModel u WHERE u.id = :userId")
	String findFullNameByUserId(@Param("userId") String userId);

	@Query("SELECT u.avatarUrl FROM UserModel u WHERE u.id = :userId")
	String findAvatarUrlByUserId(@Param("userId") String userId);

	@Query(value = "SELECT u.id, u.full_name AS fullName, u.email, u.avatar_url AS avatarUrl, u.status_id AS statusId, "
			+ "u.faculty_id AS facultyId, a.graduation_year AS graduationYear, "
			+ "(u.faculty_id = (SELECT user.faculty_id FROM user WHERE user.id = :currentUserId)) AS sameFaculty, "
			+ "(a.graduation_year = (SELECT alumni.graduation_year FROM alumni WHERE alumni.user_id = :currentUserId)) AS sameGraduationYear "
			+ "FROM user u "
			+ "LEFT JOIN alumni a ON u.id = a.user_id "
			+ "LEFT JOIN request_friend fr1 ON u.id = fr1.user_id AND fr1.friend_id = :currentUserId AND fr1.is_delete = false "
			+ "LEFT JOIN request_friend fr2 ON u.id = fr2.friend_id AND fr2.user_id = :currentUserId AND fr2.is_delete = false "
			+ "LEFT JOIN friend f1 ON u.id = f1.user_id AND :currentUserId = f1.friend_id AND f1.is_delete = false "
			+ "LEFT JOIN friend f2 ON u.id = f2.friend_id AND :currentUserId = f2.user_id AND f2.is_delete = false "
			+ "WHERE u.status_id = 2 "
			+ "AND (:fullName IS NULL OR u.full_name LIKE CONCAT('%', :fullName, '%')) "
			+ "AND (:email IS NULL OR u.email LIKE CONCAT('%', :email, '%')) "
			+ "AND u.id != :currentUserId "
			+ "AND fr1.user_id IS NULL "
			+ "AND fr2.friend_id IS NULL "
			+ "AND f1.friend_id IS NULL "
			+ "AND f2.user_id IS NULL "
			+ "ORDER BY sameFaculty DESC, sameGraduationYear DESC, u.full_name ASC", countQuery = "SELECT COUNT(u.id) "
					+ "FROM user u "
					+ "LEFT JOIN alumni a ON u.id = a.user_id "
					+ "LEFT JOIN request_friend fr1 ON u.id = fr1.user_id AND fr1.friend_id = :currentUserId AND fr1.is_delete = false "
					+ "LEFT JOIN request_friend fr2 ON u.id = fr2.friend_id AND fr2.user_id = :currentUserId AND fr2.is_delete = false "
					+ "LEFT JOIN friend f1 ON u.id = f1.user_id AND :currentUserId = f1.friend_id AND f1.is_delete = false "
					+ "LEFT JOIN friend f2 ON u.id = f2.friend_id AND :currentUserId = f2.user_id AND f2.is_delete = false "
					+ "WHERE u.status_id = 2 "
					+ "AND (:fullName IS NULL OR u.full_name LIKE CONCAT('%', :fullName, '%')) "
					+ "AND (:email IS NULL OR u.email LIKE CONCAT('%', :email, '%')) "
					+ "AND u.id != :currentUserId "
					+ "AND fr1.user_id IS NULL "
					+ "AND fr2.friend_id IS NULL "
					+ "AND f1.friend_id IS NULL "
					+ "AND f2.user_id IS NULL", nativeQuery = true)
	Page<ISuggestionUserDto> getSuggestionUsers(String fullName, String email, String currentUserId, Pageable pageable);

	@Query("SELECT DISTINCT u " + "FROM UserModel u " + "LEFT JOIN u.roles r "
			+ "WHERE (:fullName IS NULL OR u.fullName LIKE %:fullName%) "
			+ "AND (:email IS NULL OR u.email LIKE :email%) " + "AND (:roleIds IS NULL OR r.id in :roleIds) "
			+ "AND u.statusId = 2")
	Page<IUserSearchDto> searchUsers(@Param("fullName") String fullName, @Param("email") String email,
			@Param("roleIds") List<Integer> roleIds, Pageable pageable);

	@Query("SELECT COUNT(u) FROM UserModel u JOIN u.roles r WHERE r.id in :roleIds")
	Long countUsersByRoleId(@Param("roleIds") List<Integer> roleIds);

	@Query("SELECT COUNT(u) FROM UserModel u")
	Long countAllUsers();

	@Query("SELECT u FROM UserModel u")
	Page<IUserSearchDto> findAllUsers(Pageable pageable);

	@Query("SELECT u FROM UserModel u WHERE u.id = :id")
	Optional<IUserProfileDto> findUserProfileById(@Param("id") String id);

	@Transactional
	@Modifying
	@Query("UPDATE UserModel u SET u.lastLogin = :lastLogin WHERE u.email = :email")
	int setLastLogin(@Param("email") String email, @Param("lastLogin") Date lastLogin);

	@Query(value = "SELECT ur.role_id FROM user_role ur " +
			"WHERE ur.user_id = :userId", nativeQuery = true)
	Set<Integer> getRoleIds(String userId);

	@Query(value = "SELECT u.faculty_id FROM user u " +
			"WHERE u.id = :userId", nativeQuery = true)
	Integer getFacultyId(String userId);
}