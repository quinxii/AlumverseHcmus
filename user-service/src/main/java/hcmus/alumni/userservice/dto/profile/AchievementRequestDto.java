package hcmus.alumni.userservice.dto.profile;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AchievementRequestDto {
    private String achievementName;
    private String achievementType;
    private Date achievementTime;
    private String privacy;
    private Boolean isDelete = false;
}
