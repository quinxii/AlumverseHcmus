package hcmus.alumni.userservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.userservice.dto.role.IRoleDto;
import hcmus.alumni.userservice.dto.role.IRoleWithoutPermissionsDto;
import hcmus.alumni.userservice.model.RoleModel;

public interface RoleRepository extends JpaRepository<RoleModel, Integer> {
    @Query("SELECT r FROM RoleModel r " +
            "WHERE r.isDelete = false " +
            "AND (:name IS NULL OR r.name like %:name%)")
    Page<IRoleWithoutPermissionsDto> searchRoles(String name, Pageable pageable);

    @Query("SELECT r FROM RoleModel r WHERE r.id = :id AND r.isDelete = false")
    Optional<IRoleDto> findRoleById(Integer id);

    @Query("SELECT r FROM RoleModel r WHERE r.id in :ids AND r.isDelete = false")
    List<RoleModel> findByIds(List<Integer> ids);

    @Transactional
    @Modifying
    @Query("UPDATE RoleModel r SET r.isDelete = true WHERE r.id = :id AND r.isDelete = false")
    void deleteById(Integer id);

    @Query(value = "select distinct p.name from role_permission rp " +
            "join permission p on p.id = rp.permission_id and p.is_delete = false " +
            "join role r on r.id = rp.role_id and r.is_delete = false " +
            "where r.id in (select role_id from user_role where user_id = :userId) and p.name like :domain% and rp.is_delete = false;", nativeQuery = true)
    List<String> getPermissions(String userId, String domain);
    
    RoleModel findByName(String name);
}
