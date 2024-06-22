package hcmus.alumni.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.group.model.TagModel;

public interface TagRepository extends JpaRepository<TagModel, Long> {
    TagModel findByName(String name);
}
