package hcmus.alumni.userservice.dto.profile;

import java.util.Date;

import lombok.Data;

@Data
public class AchievementDto {
    private String achievementId;
    private String achievementName;
    private String achievementType;
    private Date achievementTime;
    private String privacy;
    private Boolean isDelete;
}
