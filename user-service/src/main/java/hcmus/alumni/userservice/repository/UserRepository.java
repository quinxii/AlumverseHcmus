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

import hcmus.alumni.userservice.dto.IUserProfileDto;
import hcmus.alumni.userservice.dto.UserSearchDto;
import hcmus.alumni.userservice.model.FacultyModel;
import hcmus.alumni.userservice.model.UserModel;

public interface UserRepository extends JpaRepository<UserModel, String> {
	@Query("SELECT u FROM UserModel u WHERE u.id = :id")
	Optional<UserSearchDto> findByIdCustom(String id);

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

	@Query("SELECT DISTINCT u " + "FROM UserModel u " + "LEFT JOIN u.roles r "
			+ "WHERE (:fullName IS NULL OR u.fullName LIKE %:fullName%) "
			+ "AND (:email IS NULL OR u.email LIKE :email%) " + "AND (:roleIds IS NULL OR r.id in :roleIds)")
	Page<UserSearchDto> searchUsers(@Param("fullName") String fullName, @Param("email") String email,
			@Param("roleIds") List<Integer> roleIds, Pageable pageable);

	@Query("SELECT COUNT(u) FROM UserModel u JOIN u.roles r WHERE r.id in :roleIds")
	Long countUsersByRoleId(@Param("roleIds") List<Integer> roleIds);

	@Query("SELECT COUNT(u) FROM UserModel u")
	Long countAllUsers();

	@Query("SELECT u FROM UserModel u")
	Page<UserSearchDto> findAllUsers(Pageable pageable);

	@Query("SELECT u FROM UserModel u WHERE u.id = :id")
	Optional<IUserProfileDto> findUserProfileById(@Param("id") String id);
}