package hcmus.alumni.userservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hcmus.alumni.userservice.model.VerifyAlumniModel;

public interface VerifyAlumniRepository extends JpaRepository<VerifyAlumniModel, String> {

	List<VerifyAlumniModel> findAllByIsDeleteEquals(Boolean isDelete);
	Optional<VerifyAlumniModel> findByUserIdAndIsDeleteEquals(String userId, Boolean isDelete);
}
