package hcmus.alumni.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.notification.dto.IUserSubscriptionTokenDto;
import hcmus.alumni.notification.model.user.UserSubscriptionTokenModel;
import hcmus.alumni.notification.model.user.UserSubscriptionTokenId;

public interface UserSubscriptionTokenRepository extends JpaRepository<UserSubscriptionTokenModel, UserSubscriptionTokenId> {
	@Query("SELECT token FROM UserSubscriptionTokenModel token WHERE token.id.userId = :userId AND token.isDelete = false")
	List<IUserSubscriptionTokenDto> getUserSubscriptionTokenByUserId(String userId);
	
	@Transactional
	@Modifying
	@Query(value = "UPDATE UserSubscriptionTokenModel token SET token.isDelete = true WHERE token.id.userId = :userId AND token.id.token = :token")
	int deleteUserSubscriptionToken(String userId, String token);
}
