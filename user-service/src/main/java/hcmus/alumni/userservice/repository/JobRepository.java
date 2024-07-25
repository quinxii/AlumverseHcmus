package hcmus.alumni.userservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hcmus.alumni.userservice.dto.profile.IJobDto;
import hcmus.alumni.userservice.model.JobModel;

public interface JobRepository extends JpaRepository<JobModel, String> {
	@Query("SELECT j FROM JobModel j WHERE j.userId = :userId AND j.isDelete = false")
	List<JobModel> findByUserId(String userId);

	Optional<JobModel> findByUserIdAndCompanyName(String userId, String companyName);

	Optional<IJobDto> findByJobId(String id);
	
	@Query("SELECT j FROM JobModel j WHERE j.userId = :userId AND j.companyName = :companyName AND j.position = :position")
	Optional<JobModel> findByUserIdAndCompanyNameAndPosition(String userId, String companyName, String position);

	Optional<JobModel> findByUserIdAndJobId(String id, String jobId);

}
