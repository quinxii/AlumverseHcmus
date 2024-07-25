package hcmus.alumni.userservice.dto.friend;

import java.util.Date;

public interface IFriendDto {
	
	interface Friend {
        String getId();
        String getFullName();
        String getAvatarUrl();
    }

    Date getCreateAt();
    Friend getFriend();
    Boolean getIsDelete();
}
