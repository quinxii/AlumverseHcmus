package hcmus.alumni.userservice.dto;

import hcmus.alumni.userservice.model.VerifyAlumniModel;

public interface IVerifyAlumniDto {
	interface User {
		String getFullName();
		String getEmail();
		String getAvatarUrl();
	}
	
	String getId();
	String getStudentId();
	Integer getBeginningYear();
	String getSocialMediaLink();
	VerifyAlumniModel.Status getStatus();
	String getComment();
	User getUser();
}
