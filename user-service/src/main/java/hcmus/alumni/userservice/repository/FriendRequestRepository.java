package hcmus.alumni.userservice.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hcmus.alumni.userservice.dto.IFriendRequestDto;
import hcmus.alumni.userservice.model.FriendRequestId;
import hcmus.alumni.userservice.model.FriendRequestModel;

public interface FriendRequestRepository extends JpaRepository<FriendRequestModel, FriendRequestId> {

	@Query("SELECT fr FROM FriendRequestModel fr WHERE fr.id.userId = :userId AND fr.id.friendId = :friendId")
	Optional<FriendRequestModel> findByUserIdAndFriendId(String userId, String friendId);
	
	
	@Query("SELECT fr FROM FriendRequestModel fr WHERE fr.id.userId = :userId AND fr.id.friendId = :friendId AND fr.isDelete = false")
	Optional<FriendRequestModel> findByUserIdAndFriendIdAndIsDelete(String userId, String friendId);
	
	@Query("SELECT fr FROM FriendRequestModel fr WHERE fr.id.userId = :userId AND fr.isDelete = false")
	Page<IFriendRequestDto> findByUserIdAndIsDelete(String userId, Pageable pageable);

}
