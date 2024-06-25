package hcmus.alumni.userservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.userservice.model.AlumniModel;

public interface AlumniRepository extends JpaRepository<AlumniModel, String> {
	Optional<AlumniModel> findByUserId(String id);
}
