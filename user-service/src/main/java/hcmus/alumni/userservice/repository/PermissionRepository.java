package hcmus.alumni.userservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hcmus.alumni.userservice.dto.role.IPermissionDto;
import hcmus.alumni.userservice.model.PermissionModel;

public interface PermissionRepository extends JpaRepository<PermissionModel, Integer> {
    @Query("SELECT p FROM PermissionModel p WHERE p.isDelete = false")
    List<IPermissionDto> findAllPermissions();
}
