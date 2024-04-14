package hcmus.alumni.authservice.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.authservice.model.UserModel;


public interface UserRepository extends JpaRepository<UserModel, String> {

    UserModel findByEmailAndPass(String email, String pass);
    UserModel findByEmail(String email);
    
    @Query("SELECT u FROM UserModel u WHERE u.id = :userId")
    UserModel findUserById(@Param("userId") String userId);
    
    @Transactional
    @Modifying
    @Query("UPDATE UserModel u SET u.lastLogin = :lastLogin WHERE u.email = :email")
    int setLastLogin(@Param("email") String email, @Param("lastLogin") Date lastLogin);
}

