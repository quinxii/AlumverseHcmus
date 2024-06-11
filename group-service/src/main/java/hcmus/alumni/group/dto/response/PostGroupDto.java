package hcmus.alumni.group.dto.response;

import lombok.Data;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
public class PostGroupDto {
	@Data
    public static class User {
        private String id;
        private String fullName;
        private String avatarUrl;
    }

    @Data
    public static class StatusPost {
        private String name;
    }

    @Data
    public static class Tag {
        private Integer id;
        private String name;
    }

    @Data
    public static class Pictures {
        private String id;
        private String pictureUrl;
    }

    @Data
    public static class Votes {
        @Data
        public static class Id {
            private Integer voteId;
        }

        private Id id;
        private String name;
        private Integer voteCount;
        private Boolean isVoted;
    }

    @Data
    public static class Permissions {
        private Boolean edit;
        private Boolean delete;
    }

    private String id;
    private String title;
    private List<Pictures> pictures;
    private String groupId;
    private List<Votes> votes;
    private String content;
    private Integer childrenCommentNumber;
    private Integer reactionCount;
    private Date updateAt;
    private Date publishedAt;
    private User creator;
    private Set<Tag> tags;
    private StatusPost status;
    private Boolean isReacted;
    private Boolean allowMultipleVotes;
    private Boolean allowAddOptions;
    private Permissions permissions;
}
