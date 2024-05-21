package hcmus.alumni.authservice.repository;

import hcmus.alumni.authservice.model.PasswordResetModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetRepository extends JpaRepository<PasswordResetModel, Long> {
    PasswordResetModel findByEmail(String email);
}
