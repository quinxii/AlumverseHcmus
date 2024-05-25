package hcmus.alumni.authservice.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.authservice.model.UserModel;

public interface UserRepository extends JpaRepository<UserModel, String> {
	Optional<UserModel> findById(String id);

    UserModel findByEmailAndPass(String email, String pass);
    UserModel findByEmail(String email);
    
    @Query("SELECT u FROM UserModel u WHERE u.id = :userId")
    UserModel findUserById(@Param("userId") String userId);
    
    @Transactional
    @Modifying
    @Query("UPDATE UserModel u SET u.lastLogin = :lastLogin WHERE u.email = :email")
    int setLastLogin(@Param("email") String email, @Param("lastLogin") Date lastLogin);
    
    @Query(value = "select distinct p.name from role_permission rp " +
            "join role r on r.id = rp.role_id and r.is_delete = false " +
            "join permission p on p.id = rp.permission_id and p.is_delete = false " +
            "where r.name in :role and p.name like :domain% and rp.is_delete = false", nativeQuery = true)
    List<String> getPermissions(List<String> role, String domain);
}

