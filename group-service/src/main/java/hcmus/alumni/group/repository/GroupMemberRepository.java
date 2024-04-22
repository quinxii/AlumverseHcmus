package hcmus.alumni.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import hcmus.alumni.group.model.GroupMemberModel;

public interface GroupMemberRepository extends JpaRepository<GroupMemberModel, String> {        

}
