package hcmus.alumni.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import hcmus.alumni.userservice.model.StatusUserGroupModel;

public interface StatusUserGroupRepository extends JpaRepository<StatusUserGroupModel, Integer> {
}
