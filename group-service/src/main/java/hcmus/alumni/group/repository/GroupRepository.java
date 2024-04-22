package hcmus.alumni.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import hcmus.alumni.group.model.GroupModel;

public interface GroupRepository extends JpaRepository<GroupModel, String> {        

}
