package hcmus.alumni.userservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.userservice.dto.IUserSubscriptionTokenDto;
import hcmus.alumni.userservice.model.UserSubscriptionTokenModel;

public interface UserSubscriptionTokenRepository extends JpaRepository<UserSubscriptionTokenModel, String> {
	@Query("SELECT token FROM UserSubscriptionTokenModel token WHERE token.userId = :userId AND token.isDelete = false")
	List<IUserSubscriptionTokenDto> getUserSubscriptionTokenByUserId(String userId);
	
	@Transactional
	@Modifying
	@Query(value = "UPDATE UserSubscriptionTokenModel SET isDelete = 1 WHERE id = :id")
	int deleteUserSubscriptionTokenById(String id);
}
