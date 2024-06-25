package hcmus.alumni.group.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hcmus.alumni.group.model.UserModel;

public interface UserRepository extends JpaRepository<UserModel, String> {}
