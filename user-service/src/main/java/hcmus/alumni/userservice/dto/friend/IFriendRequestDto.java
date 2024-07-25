package hcmus.alumni.userservice.dto.friend;

import java.util.Date;

public interface IFriendRequestDto {
	
	interface User {
        String getId();
        String getFullName();
        String getAvatarUrl();
    }

    Date getCreateAt();
    User getUser();
    Boolean getIsDelete();
}
