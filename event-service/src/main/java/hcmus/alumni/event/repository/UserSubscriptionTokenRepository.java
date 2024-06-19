package hcmus.alumni.event.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hcmus.alumni.event.model.UserSubscriptionTokenModel;

public interface UserSubscriptionTokenRepository extends JpaRepository<UserSubscriptionTokenModel, String> {
	@Query("SELECT ust.token FROM UserSubscriptionTokenModel ust WHERE ust.userId = :userId AND ust.isDelete = false")
    List<String> findAllTokensByUserId(@Param("userId") String userId);
}
