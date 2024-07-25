package hcmus.alumni.userservice.dto.profile;

import java.util.Date;

import lombok.Data;

@Data
public class EducationDto {
    private String educationId;
    private String schoolName;
    private String degree;
    private Date startTime;
    private Date endTime;
    private String privacy;
    private Boolean isDelete;
    private Boolean isLearning;
}
