package hcmus.alumni.counsel.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hcmus.alumni.counsel.dto.response.IInteractPostAdviseDto;
import hcmus.alumni.counsel.model.InteractPostAdviseId;
import hcmus.alumni.counsel.model.InteractPostAdviseModel;

public interface InteractPostAdviseRepository extends JpaRepository<InteractPostAdviseModel, InteractPostAdviseId> {
    @Query("SELECT ipa FROM InteractPostAdviseModel ipa " +
            "WHERE ipa.id.postAdviseId = :postAdviseId AND ipa.react.id = :reactId AND ipa.isDelete = false")
    Page<IInteractPostAdviseDto> getReactionUsers(String postAdviseId, Integer reactId, Pageable pageable);
}
