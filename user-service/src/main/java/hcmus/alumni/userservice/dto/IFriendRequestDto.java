package hcmus.alumni.userservice.dto;

import java.util.Date;

public interface IFriendRequestDto {
	
	interface FriendRequest {
        String getId();
        String getFullName();
        String getAvatarUrl();
    }

    Date getCreateAt();
    FriendRequest getFriend();
    Boolean getIsDelete();
}
