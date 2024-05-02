package hcmus.alumni.halloffame.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import hcmus.alumni.halloffame.model.UserModel;

public interface UserRepository extends JpaRepository<UserModel, String> {
    UserModel findByEmail(String email);
}
