package hcmus.alumni.counsel.dto;

import java.util.Date;

public interface ICommentPostAdviseDto {
    interface User {
        String getId();
        String getFullName();
        String getAvatarUrl();
    }
    interface Permissions {
        Boolean getEdit();
        Boolean getDelete();
    }

    String getId();
    User getCreator();
    String getContent();
    Integer getChildrenCommentNumber();
    Date getCreateAt();
    Date getUpdateAt();
    Permissions getPermissions();
}