package hcmus.alumni.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.authservice.model.EmailActivationCodeModel;


public interface EmailActivationCodeRepository extends JpaRepository<EmailActivationCodeModel, String> {
	
	EmailActivationCodeModel findByEmail(String email);
	EmailActivationCodeModel findByActivationCode(String activationCode);

}
