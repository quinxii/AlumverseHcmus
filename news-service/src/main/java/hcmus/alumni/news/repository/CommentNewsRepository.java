package hcmus.alumni.news.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hcmus.alumni.news.dto.ICommentNewsDto;
import hcmus.alumni.news.model.CommentNewsModel;

public interface CommentNewsRepository extends JpaRepository<CommentNewsModel, String> {

    @Query("SELECT c FROM CommentNewsModel c WHERE c.news.id = :newsId AND c.isDelete = false AND c.parentId IS NULL")
    Page<ICommentNewsDto> getComments(String newsId, Pageable pageable);

    @Query("SELECT c FROM CommentNewsModel c WHERE c.isDelete = false AND c.parentId = :commentId")
    Page<ICommentNewsDto> getChildrenComment(String commentId, Pageable pageable);
}
