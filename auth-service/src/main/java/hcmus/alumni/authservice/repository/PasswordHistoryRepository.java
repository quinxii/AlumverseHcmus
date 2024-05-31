package hcmus.alumni.authservice.repository;

import hcmus.alumni.authservice.model.PasswordHistoryModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistoryModel, String> {

	PasswordHistoryModel findByUserId(String id);
}
