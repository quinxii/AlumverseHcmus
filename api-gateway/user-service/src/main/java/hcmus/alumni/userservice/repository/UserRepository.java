package hcmus.alumni.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hcmus.alumni.userservice.model.UserModel;

public interface UserRepository extends JpaRepository<UserModel, String> {

    UserModel findByEmailAndPass(String email, String pass);
    
    @Query("SELECT u FROM UserModel u WHERE u.id = :userId")
    UserModel findUserById(@Param("userId") String userId);

}


