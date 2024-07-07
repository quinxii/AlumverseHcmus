package hcmus.alumni.userservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.userservice.dto.ISuggestionUserDto;
import hcmus.alumni.userservice.dto.IUserProfileDto;
import hcmus.alumni.userservice.dto.IUserSearchDto;
import hcmus.alumni.userservice.model.FacultyModel;
import hcmus.alumni.userservice.model.UserModel;

public interface UserRepository extends JpaRepository<UserModel, String> {
	@Query("SELECT u FROM UserModel u WHERE u.id = :id")
	Optional<IUserSearchDto> findByIdCustom(String id);

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

	@Transactional
	@Modifying
	@Query(value = "UPDATE user_role SET role_id = 4 WHERE user_id = :userId AND role_id = 5", nativeQuery = true)
	int updateRoleFromGuestToAlumni(String userId);

	@Query("SELECT u.fullName FROM UserModel u WHERE u.id = :userId")
	String findFullNameByUserId(@Param("userId") String userId);

	@Query("SELECT u.avatarUrl FROM UserModel u WHERE u.id = :userId")
	String findAvatarUrlByUserId(@Param("userId") String userId);

	@Query(value = "SELECT u.id, u.full_name AS fullName, u.email, u.avatar_url AS avatarUrl, u.status_id AS statusId "
			+ "FROM user u "
			+ "LEFT JOIN request_friend fr ON u.id = fr.user_id AND fr.friend_id = :currentUserId AND fr.is_delete = false "
			+ "LEFT JOIN friend f ON u.id = f.friend_id AND f.is_delete = false "
			+ "WHERE (:fullName IS NULL OR u.full_name LIKE CONCAT('%', :fullName, '%')) "
			+ "AND (:email IS NULL OR u.email LIKE CONCAT('%', :email, '%')) " + "AND u.id != :currentUserId "
			+ "AND fr.user_id IS NULL " + "AND f.friend_id IS NULL", countQuery = "SELECT COUNT(u.id) " + "FROM user u "
					+ "LEFT JOIN request_friend fr ON u.id = fr.user_id AND fr.friend_id = :currentUserId AND fr.is_delete = false "
					+ "LEFT JOIN friend f ON u.id = f.friend_id AND f.is_delete = false "
					+ "WHERE (:fullName IS NULL OR u.full_name LIKE CONCAT('%', :fullName, '%')) "
					+ "AND (:email IS NULL OR u.email LIKE CONCAT('%', :email, '%')) " + "AND u.id != :currentUserId "
					+ "AND fr.user_id IS NULL " + "AND f.friend_id IS NULL", nativeQuery = true)
	Page<ISuggestionUserDto> getSuggestionUsers(String fullName, String email, String currentUserId, Pageable pageable);

	@Query("SELECT DISTINCT u " + "FROM UserModel u " + "LEFT JOIN u.roles r "
			+ "WHERE (:fullName IS NULL OR u.fullName LIKE %:fullName%) "
			+ "AND (:email IS NULL OR u.email LIKE :email%) " + "AND (:roleIds IS NULL OR r.id in :roleIds)")
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
}