package hcmus.alumni.group.dto;

public interface IInteractPostGroupDto {
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
