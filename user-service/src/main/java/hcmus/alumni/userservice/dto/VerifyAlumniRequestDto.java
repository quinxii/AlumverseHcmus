package hcmus.alumni.userservice.dto;

import lombok.Data;

@Data
public class VerifyAlumniRequestDto {
    private String studentId;
    private Integer beginningYear;
}
