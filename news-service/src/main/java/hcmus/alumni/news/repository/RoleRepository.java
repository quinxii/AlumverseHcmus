package hcmus.alumni.news.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hcmus.alumni.news.model.RoleModel;

public interface RoleRepository extends JpaRepository<RoleModel, Integer> {
    @Query(value = "select distinct p.name from role_permission rp " +
            "join role r on r.id = rp.role_id " +
            "join permission p on p.id = rp.permission_id " +
            "where r.name in :role and p.name like :domain%", nativeQuery = true)
    List<String> getPermissions(List<String> role, String domain);
}
