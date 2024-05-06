package hcmus.alumni.counsel.dto;

public interface IInteractPostAdviseDto {
    interface Creator {
        String getId();
        String getFullName();
        String getAvatarUrl();
    }

    interface React {
        Integer getId();
        String getName();
    }

    Creator getCreator();
    React getReact();
}
