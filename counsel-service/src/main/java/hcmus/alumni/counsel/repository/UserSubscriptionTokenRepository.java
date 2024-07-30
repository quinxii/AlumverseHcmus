package hcmus.alumni.counsel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hcmus.alumni.counsel.model.UserSubscriptionTokenId;
import hcmus.alumni.counsel.model.UserSubscriptionTokenModel;

public interface UserSubscriptionTokenRepository extends JpaRepository<UserSubscriptionTokenModel, UserSubscriptionTokenId> {
	@Query("SELECT ust.id.token FROM UserSubscriptionTokenModel ust WHERE ust.id.userId = :userId AND ust.isDelete = false")
    List<String> findAllTokensByUserId(@Param("userId") String userId);
}
