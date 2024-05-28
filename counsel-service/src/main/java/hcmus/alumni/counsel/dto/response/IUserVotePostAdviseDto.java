package hcmus.alumni.counsel.dto.response;

public interface IUserVotePostAdviseDto {
    interface User {
        String getId();
        String getFullName();
        String getAvatarUrl();
    }

    User getUser();
}
