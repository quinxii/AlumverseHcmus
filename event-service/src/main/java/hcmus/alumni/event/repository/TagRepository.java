package hcmus.alumni.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.event.model.TagModel;

public interface TagRepository extends JpaRepository<TagModel, Long> {
    TagModel findByName(String name);
}
