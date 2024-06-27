package hcmus.alumni.news.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.news.model.UserModel;

public interface UserRepository extends JpaRepository<UserModel, String> {}
