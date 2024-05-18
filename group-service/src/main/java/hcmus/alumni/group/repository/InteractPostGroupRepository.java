package hcmus.alumni.group.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hcmus.alumni.group.dto.IInteractPostGroupDto;
import hcmus.alumni.group.model.InteractPostGroupId;
import hcmus.alumni.group.model.InteractPostGroupModel;

public interface InteractPostGroupRepository extends JpaRepository<InteractPostGroupModel, InteractPostGroupId> {
    @Query("SELECT ip FROM InteractPostGroupModel ip " +
            "WHERE ip.id.postGroupId = :postGroupId AND ip.react.id = :reactId AND ip.isDelete = false")
    Page<IInteractPostGroupDto> getReactionUsers(String postGroupId, Integer reactId, Pageable pageable);
}
