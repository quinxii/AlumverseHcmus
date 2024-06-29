package hcmus.alumni.userservice.dto;

import java.util.Date;

public interface IAchievementDto {
	String getAchievementId();
    String getName();
    String getType();
    Date getTime();
    String getPrivacy();
    Boolean getIsDelete();
}

