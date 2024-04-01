package hcmus.alumni.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.event.model.UserModel;

public interface UserRepository extends JpaRepository<UserModel, String> {

	
}
