package hcmus.alumni.news.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.news.model.TagModel;

public interface TagRepository extends JpaRepository<TagModel, Long> {
    TagModel findByName(String name);
}
