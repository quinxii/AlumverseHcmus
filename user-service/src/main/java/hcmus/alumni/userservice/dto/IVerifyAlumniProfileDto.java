package hcmus.alumni.userservice.dto;

import hcmus.alumni.userservice.model.VerifyAlumniModel;

public interface IVerifyAlumniProfileDto {
	VerifyAlumniModel.Status getStatus();
    String getStudentId();
    Integer getBeginningYear();
}
