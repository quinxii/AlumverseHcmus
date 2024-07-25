package hcmus.alumni.userservice.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hcmus.alumni.userservice.dto.friend.FriendRelationShipDto;
import hcmus.alumni.userservice.model.FriendId;
import hcmus.alumni.userservice.model.FriendModel;

public interface FriendRepository extends JpaRepository<FriendModel, FriendId> {
	@Query("SELECT f " + "FROM FriendModel f " + "JOIN UserModel u1 ON f.id.userId = u1.id "
			+ "JOIN UserModel u2 ON f.id.friendId = u2.id "
			+ "WHERE (f.id.userId = :userId OR f.id.friendId = :userId) AND f.isDelete = false")
	Page<FriendModel> getAllUserFriends(String userId, Pageable pageable);

	@Query(value = "select count(*) from friend f "
			+ "where (f.user_id = :userId or f.friend_id = :userId)and f.is_delete = false", nativeQuery = true)
	Long countFriendByUserId(String userId);

	@Query("SELECT DISTINCT f " + "FROM FriendModel f " + "JOIN UserModel u1 ON f.id.userId = u1.id "
			+ "JOIN UserModel u2 ON f.id.friendId = u2.id "
			+ "WHERE (f.id.userId = :userId OR f.id.friendId = :userId) AND f.isDelete = false "
			+ "AND (:fullName IS NULL OR u1.fullName LIKE %:fullName% OR u2.fullName LIKE %:fullName%)")
	Page<FriendModel> getUserFriendsByFullName(String userId, String fullName, Pageable pageable);

	@Query("SELECT f FROM FriendModel f WHERE ((f.id.userId = :userId AND f.id.friendId = :friendId) or (f.id.userId = :friendId AND f.id.friendId = :userId) ) AND f.isDelete = false")
	Optional<FriendModel> findByUserIdAndFriendIdAndIsDelete(String userId, String friendId);

}