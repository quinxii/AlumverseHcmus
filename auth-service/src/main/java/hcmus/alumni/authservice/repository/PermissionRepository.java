package hcmus.alumni.authservice.repository;

import hcmus.alumni.authservice.dto.PermissionNameOnly;
import hcmus.alumni.authservice.model.PermissionModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PermissionRepository extends CrudRepository<PermissionModel, Integer> {
	@Query("SELECT p.name AS name FROM PermissionModel p JOIN RolePermissionModel rp ON rp.id.permissionId = p.id WHERE rp.id.roleId IN :roleIds")
    List<PermissionNameOnly> findPermissionNamesByRoleIds(@Param("roleIds") List<Integer> roleIds);
}
