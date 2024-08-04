package hcmus.alumni.halloffame.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hcmus.alumni.halloffame.model.UserModel;

public interface UserRepository extends JpaRepository<UserModel, String> {
    UserModel findByEmail(String email);

    @Query(value = "SELECT ur.role_id FROM user_role ur " +
            "WHERE ur.user_id = :userId", nativeQuery = true)
    Set<Integer> getRoleIds(String userId);
}
