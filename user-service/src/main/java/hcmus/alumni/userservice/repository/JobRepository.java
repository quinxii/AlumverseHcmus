package hcmus.alumni.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import hcmus.alumni.userservice.model.JobModel;

public interface JobRepository extends JpaRepository<JobModel, String> {

    Optional<JobModel> findByUserId(String id);
    Optional<JobModel> findByUserIdAndCompanyName(String userId, String companyName);
    Optional<JobModel> findByCompanyName(String companyName);
    
}
