package hcmus.alumni.userservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.userservice.model.FollowUserId;
import hcmus.alumni.userservice.model.FollowUserModel;

public interface FollowUserRepository extends JpaRepository<FollowUserModel, FollowUserId> {
    boolean existsByIdUserIdAndIdFollowerId(String userId, String followerId);
    Optional<FollowUserModel> findByIdUserIdAndIdFollowerIdAndIsDelete(String userId, String followerId, boolean isDelete);
}