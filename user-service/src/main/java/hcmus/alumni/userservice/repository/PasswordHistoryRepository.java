package hcmus.alumni.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.userservice.model.PasswordHistoryModel;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistoryModel, String> {

	PasswordHistoryModel findByUserId(String id);
}
