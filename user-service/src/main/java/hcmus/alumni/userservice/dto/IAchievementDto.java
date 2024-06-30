package hcmus.alumni.userservice.dto;

import java.util.Date;

public interface IAchievementDto {
	String getAchievementId();
    String getAchievementName();
    String getAchievementType();
    Date getAchievementTime();
    String getPrivacy();
    Boolean getIsDelete();
}

