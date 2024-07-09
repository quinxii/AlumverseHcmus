package hcmus.alumni.userservice.dto;

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
