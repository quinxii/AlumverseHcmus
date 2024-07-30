package hcmus.alumni.counsel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.counsel.model.TagModel;

public interface TagRepository extends JpaRepository<TagModel, Long> {
    TagModel findByName(String name);
}
