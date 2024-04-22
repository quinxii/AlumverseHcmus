package hcmus.alumni.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import hcmus.alumni.group.model.PostGroupModel;

public interface PostGroupRepository extends JpaRepository<PostGroupModel, String> {        

}
