package hcmus.alumni.userservice.dto;

import java.util.Date;

public interface IEducationDto {
	String getEducationId();
    String getSchoolName();
    String getDegree();
    Date getStartTime();
    Date getEndTime();
    String getPrivacy();
    Boolean getIsDelete();
    Boolean getIsLearning();
}

