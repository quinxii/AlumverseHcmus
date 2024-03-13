package hcmus.alumni.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.userservice.model.UserModel;

public interface UserRepository extends JpaRepository<UserModel, String> {

    UserModel findByEmailAndPass(String email, String pass);
    
    @Query("SELECT u FROM UserModel u WHERE u.id = :userId")
    UserModel findUserById(@Param("userId") String userId);

    @Transactional
    @Modifying
    @Query("UPDATE UserModel u SET u.avatarUrl = :avatarUrl WHERE u.id = :userId")
    int setAvatarUrl(@Param("userId") String userId, @Param("avatarUrl") String avatarUrl);
}


