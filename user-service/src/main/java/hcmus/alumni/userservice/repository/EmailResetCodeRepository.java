package hcmus.alumni.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.userservice.model.EmailResetCodeModel;

public interface EmailResetCodeRepository extends JpaRepository<EmailResetCodeModel, String> {

	EmailResetCodeModel findByEmail(String email);

	EmailResetCodeModel findByResetCode(String resetCode);

}