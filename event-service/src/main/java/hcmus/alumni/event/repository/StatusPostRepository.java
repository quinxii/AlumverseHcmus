package hcmus.alumni.event.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.event.model.StatusPost;

public interface StatusPostRepository extends JpaRepository<StatusPost, Integer> {
	

	
}
