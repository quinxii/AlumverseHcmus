package hcmus.alumni.halloffame.dto;

public interface IHallOfFameDto {

	interface StatusPost{
		String getName();
	}
	
	String getId();
	String getTitle();
	String getContent();
	String getThumbnail();
	Integer getViews();
	StatusPost getStatus();
	String getFaculty();
	Integer getBeginningYear();
}