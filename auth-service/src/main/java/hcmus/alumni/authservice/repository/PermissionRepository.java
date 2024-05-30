package hcmus.alumni.authservice.repository;

import hcmus.alumni.authservice.dto.PermissionNameOnly;
import hcmus.alumni.authservice.model.PermissionModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PermissionRepository extends CrudRepository<PermissionModel, Integer> {

    @Query("SELECT p.name AS name FROM PermissionModel p JOIN p.roles r WHERE r.id IN :roles")
    List<PermissionNameOnly> findPermissionNamesByRoleIds(@Param("roles") List<Integer> roles);
}
