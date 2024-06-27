package hcmus.alumni.counsel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.counsel.model.UserModel;

public interface UserRepository extends JpaRepository<UserModel, String> {}
