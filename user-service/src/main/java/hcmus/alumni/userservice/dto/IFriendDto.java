package hcmus.alumni.userservice.dto;

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
