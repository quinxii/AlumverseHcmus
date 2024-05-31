package hcmus.alumni.userservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.userservice.dto.IVerifyAlumniDto;
import hcmus.alumni.userservice.model.VerifyAlumniModel;

public interface VerifyAlumniRepository extends JpaRepository<VerifyAlumniModel, String> {
	long countByIsDeleteEqualsAndStatusEquals(Boolean isDelete, VerifyAlumniModel.Status status);

	long countByIsDeleteEqualsAndStatusNot(Boolean isDelete, VerifyAlumniModel.Status status);

	List<IVerifyAlumniDto> findAllByIsDeleteEqualsAndStatusNot(Boolean isDelete, VerifyAlumniModel.Status status,
			Pageable pageable);

	Optional<VerifyAlumniModel> findByUserIdAndIsDeleteEquals(String userId, Boolean isDelete);

	VerifyAlumniModel findByIdAndIsDeleteEqualsAndStatusEquals(String id, Boolean isDelete,
			VerifyAlumniModel.Status status);
}