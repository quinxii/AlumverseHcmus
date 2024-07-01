package hcmus.alumni.userservice.dto;

import java.util.Date;


public interface IFriendDto {
    String getUserId();
    String getFriendId();
    Date getCreateAt();
    Boolean getIsDelete();
}
