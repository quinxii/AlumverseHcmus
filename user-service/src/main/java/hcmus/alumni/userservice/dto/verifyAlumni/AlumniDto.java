package hcmus.alumni.userservice.dto.verifyAlumni;
import lombok.Data;

@Data
public class AlumniDto {
	private String studentId;
	private String alumClass;
	private Integer beginningYear;
    private Integer graduationYear;
}
