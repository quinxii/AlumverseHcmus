package hcmus.alumni.search.dto;

public interface ISearchDto {
    interface User {
        String getId(); // Added
        String getFullName();
        Alumni getAlumni();
    }

    interface Faculty {
        Integer getId();
        String getName();
    }
    
    interface StatusUser {
        Integer getId(); // Added
        String getName();
    }
    
    interface Alumni {
        Integer getBeginningYear(); 
    }

    String getId();
    String getFullName();
    String getAvatarUrl();
    Faculty getFaculty();
    String getSocialMediaLink();
    Alumni getAlumni();

}
