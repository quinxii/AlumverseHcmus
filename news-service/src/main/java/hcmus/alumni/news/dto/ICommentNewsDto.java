package hcmus.alumni.news.dto;

import java.util.Date;

public interface ICommentNewsDto {
    interface User {
        String getId();
        String getFullName();
        String getAvatarUrl();
    }

    String getId();
    User getCreator();
    String getContent();
    Integer getChildrenCommentNumber();
    Date getCreateAt();
    Date getUpdateAt();
}