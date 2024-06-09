package hcmus.alumni.group.dto.response;

public interface IUserVotePostGroupDto {
    interface User {
        String getId();
        String getFullName();
        String getAvatarUrl();
    }

    User getUser();
}
