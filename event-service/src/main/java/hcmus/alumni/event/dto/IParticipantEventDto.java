package hcmus.alumni.event.dto;

public interface IParticipantEventDto {
	interface Id {
		String getEventId();
		String getUserId();
    }
	
	interface Permissions {
        Boolean getDelete();
    }
	
	Id getId();
	String getFullName();
	String getAvatarUrl();
	String getNote();
	Permissions getPermissions();
}
