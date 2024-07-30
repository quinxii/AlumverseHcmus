package hcmus.alumni.group.dto.response;

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
