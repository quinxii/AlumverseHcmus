package hcmus.alumni.userservice.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AchievementDto {
    private String name;
    private String type;
    private Date time;
    private String privacy;
    private Boolean isDelete;
}
