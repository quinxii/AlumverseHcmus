package hcmus.alumni.counsel.dto.response;

import java.util.Date;

public interface ICommentWithPostDto {
    interface PostAdvise {
        String getId();
        String getTitle();
    }
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
    PostAdvise getPostAdvise();
    String getParentId();
    String getContent();
    Integer getChildrenCommentNumber();
    Date getCreateAt();
    Date getUpdateAt();
    Permissions getPermissions();
}
