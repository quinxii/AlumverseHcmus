package hcmus.alumni.group.dto.response;

import java.util.Date;

import lombok.Data;

@Data
public class CommentPostGroupDto {
    @Data
    static class User {
        String id;
        String fullName;
        String avatarUrl;
    }

    @Data
    static class Permissions {
        Boolean edit;
        Boolean delete;
    }

    String id;
    User creator;
    String parentId;
    String content;
    Integer childrenCommentNumber;
    Date createAt;
    Date updateAt;
    Permissions permissions;
}