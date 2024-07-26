package hcmus.alumni.userservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hcmus.alumni.userservice.dto.role.IPermissionDto;
import hcmus.alumni.userservice.model.PermissionModel;

public interface PermissionRepository extends JpaRepository<PermissionModel, Integer> {
    @Query("SELECT p FROM PermissionModel p WHERE p.isDelete = false")
    List<IPermissionDto> findAllPermissions();

    @Query(value = "select distinct p.name from permission p " +
            "join role_permission rp on p.id = rp.permission_id " +
            "join role r on r.id = rp.role_id " +
            "where rp.role_id in :roleIds and r.is_delete = false and p.is_delete = false and rp.is_delete = false", nativeQuery = true)
    List<String> getPermissionNamesByRoleIds(@Param("roleIds") List<Integer> roleIds);
}
