package hcmus.alumni.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.authservice.model.EmailResetCodeModel;


public interface EmailResetCodeRepository extends JpaRepository<EmailResetCodeModel, String> {
	
	EmailResetCodeModel findByEmail(String email);
	EmailResetCodeModel findByResetCode(String resetCode);

}