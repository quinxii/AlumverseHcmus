package hcmus.alumni.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import hcmus.alumni.authservice.model.RoleModel;

public interface RoleRepository extends JpaRepository<RoleModel, Integer> {
    RoleModel findByName(String name);
}