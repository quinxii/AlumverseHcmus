package hcmus.alumni.userservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hcmus.alumni.userservice.dto.IVerifyAlumniDto;
import hcmus.alumni.userservice.dto.IVerifyAlumniProfileDto;
import hcmus.alumni.userservice.model.VerifyAlumniModel;
import hcmus.alumni.userservice.model.VerifyAlumniModel.Status;

public interface VerifyAlumniRepository extends JpaRepository<VerifyAlumniModel, String> {
	long countByIsDeleteEqualsAndStatusEquals(Boolean isDelete, VerifyAlumniModel.Status status);

	long countByIsDeleteEqualsAndStatusNot(Boolean isDelete, VerifyAlumniModel.Status status);

	List<IVerifyAlumniDto> findAllByIsDeleteEqualsAndStatusNot(Boolean isDelete, VerifyAlumniModel.Status status,
			Pageable pageable);

	Optional<VerifyAlumniModel> findByUserIdAndIsDeleteEquals(String userId, Boolean isDelete);

	@Query("SELECT v.status AS status, v.studentId AS studentId, v.beginningYear AS beginningYear "
			+ "FROM VerifyAlumniModel v WHERE v.user.id = :userId AND v.isDelete = :isDelete")
	Optional<IVerifyAlumniProfileDto> findByUserIdAndIsDelete(@Param("userId") String userId,
			@Param("isDelete") boolean isDelete);

	VerifyAlumniModel findByIdAndIsDeleteEqualsAndStatusEquals(String id, Boolean isDelete,
			VerifyAlumniModel.Status status);

	Optional<VerifyAlumniModel> findByUserIdAndStatusAndIsDeleteEquals(String id, Status pending, boolean b);

	Optional<VerifyAlumniModel> findTopByUserIdAndIsDeleteEqualsOrderByCreateAtDesc(String userId, boolean isDelete);
}