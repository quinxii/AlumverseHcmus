package hcmus.alumni.userservice.dto.profile;

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

