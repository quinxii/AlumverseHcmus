package hcmus.alumni.userservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.userservice.dto.IRoleDto;
import hcmus.alumni.userservice.model.RoleModel;

public interface RoleRepository extends JpaRepository<RoleModel, Integer> {
    @Query("SELECT r FROM RoleModel r WHERE r.isDelete = false")
    List<IRoleDto> findAllRoles();

    @Query("SELECT r FROM RoleModel r WHERE r.id = :id AND r.isDelete = false")
    Optional<IRoleDto> findRoleById(Integer id);

    @Transactional
    @Modifying
    @Query("UPDATE RoleModel r SET r.isDelete = true WHERE r.id = :id AND r.isDelete = false")
    void deleteById(Integer id);

    @Query(value = "select distinct p.name from role_permission rp " +
            "join role r on r.id = rp.role_id and r.is_delete = false " +
            "join permission p on p.id = rp.permission_id and p.is_delete = false " +
            "where r.name in :role and p.name like :domain% and rp.is_delete = false", nativeQuery = true)
    List<String> getPermissions(List<String> role, String domain);
}
