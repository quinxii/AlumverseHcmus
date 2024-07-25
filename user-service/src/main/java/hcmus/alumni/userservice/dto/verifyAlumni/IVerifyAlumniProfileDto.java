package hcmus.alumni.userservice.dto.verifyAlumni;

import hcmus.alumni.userservice.model.VerifyAlumniModel;

public interface IVerifyAlumniProfileDto {
	VerifyAlumniModel.Status getStatus();
    String getStudentId();
    Integer getBeginningYear();
}
