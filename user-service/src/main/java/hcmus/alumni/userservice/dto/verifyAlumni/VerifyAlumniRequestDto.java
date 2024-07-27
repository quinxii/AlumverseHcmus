package hcmus.alumni.userservice.dto.verifyAlumni;

import hcmus.alumni.userservice.model.FacultyModel;
import lombok.Data;

@Data
public class VerifyAlumniRequestDto {
    private String studentId;
    private Integer beginningYear;
    private Integer facultyId;
    private String socialMediaLink;
}
