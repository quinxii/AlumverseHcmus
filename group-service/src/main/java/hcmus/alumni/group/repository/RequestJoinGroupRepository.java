package hcmus.alumni.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import hcmus.alumni.group.model.RequestJoinGroupModel;

public interface RequestJoinGroupRepository extends JpaRepository<RequestJoinGroupModel, String> {        

}
