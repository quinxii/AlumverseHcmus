package hcmus.alumni.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.userservice.model.FriendRequestModel;

public interface FriendRequestRepository extends JpaRepository<FriendRequestModel, String> {
	boolean existsByIdUserIdAndIdFriendId(String userId, String friendId);
}
