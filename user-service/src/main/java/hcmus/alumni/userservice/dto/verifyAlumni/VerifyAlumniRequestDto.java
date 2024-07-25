package hcmus.alumni.userservice.dto.verifyAlumni;

import lombok.Data;

@Data
public class VerifyAlumniRequestDto {
    private String studentId;
    private Integer beginningYear;
}
