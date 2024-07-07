package hcmus.alumni.userservice.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hcmus.alumni.userservice.dto.IFriendDto;
import hcmus.alumni.userservice.model.FriendId;
import hcmus.alumni.userservice.model.FriendModel;
import hcmus.alumni.userservice.model.FriendRequestModel;

public interface FriendRepository extends JpaRepository<FriendModel, FriendId> {
	// Add or f.friend_id = :userId
	@Query("SELECT f " +
		       "FROM FriendModel f " +
		       "WHERE (f.id.userId = :userId OR f.id.friendId = :userId) " +
		       "AND f.isDelete = false")
		Page<IFriendDto> getAllUserFriends(String userId, Pageable pageable);
	// Add or f.friend_id = :userId
	@Query(value = "select count(*) from friend f "
			+ "where (f.user_id = :userId or f.friend_id = :userId)and f.is_delete = false", nativeQuery = true)
	Long countFriendByUserId(String userId);
	// Add JOIN UserModel u2 ON f.id.userId = u2.id 
	// Add OR f.id.friendId = :userId
	@Query("SELECT DISTINCT f " +
		       "FROM FriendModel f " +
		       "JOIN UserModel u1 ON f.id.friendId = u1.id " +
		       "JOIN UserModel u2 ON f.id.userId = u2.id " +
		       "WHERE (f.id.userId = :userId OR f.id.friendId = :userId) " +
		       "AND f.isDelete = false " +
		       "AND (:fullName IS NULL OR u1.fullName LIKE %:fullName%)")
	Page<IFriendDto> getUserFriendsByFullName(String userId, String fullName, Pageable pageable);
	// Add or (f.id.userId = :friendId AND f.id.friendId = :userId) 
	@Query("SELECT f FROM FriendModel f WHERE ((f.id.userId = :userId AND f.id.friendId = :friendId) or (f.id.userId = :friendId AND f.id.friendId = :userId) ) AND f.isDelete = false")
	Optional<FriendModel> findByUserIdAndFriendIdAndIsDelete(String userId, String friendId);
	
}