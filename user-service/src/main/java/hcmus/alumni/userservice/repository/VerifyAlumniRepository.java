package hcmus.alumni.userservice.repository;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hcmus.alumni.userservice.dto.IVerifyAlumniDto;
import hcmus.alumni.userservice.model.VerifyAlumniModel;

public interface VerifyAlumniRepository extends JpaRepository<VerifyAlumniModel, String> {
	long countByIsDeleteEqualsAndStatusEquals(Boolean isDelete, VerifyAlumniModel.Status status);
	long countByIsDeleteEqualsAndStatusNot(Boolean isDelete, VerifyAlumniModel.Status status);

	List<IVerifyAlumniDto> findAllByIsDeleteEqualsAndStatusNot(Boolean isDelete, VerifyAlumniModel.Status status,
			Pageable pageable);

	Optional<VerifyAlumniModel> findByUserIdAndIsDeleteEquals(String userId, Boolean isDelete);

	VerifyAlumniModel findByIdAndIsDeleteEquals(String id, Boolean isDelete);
}
