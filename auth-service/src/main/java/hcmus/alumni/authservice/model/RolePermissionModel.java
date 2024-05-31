package hcmus.alumni.authservice.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "[role_permission]")
public class RolePermissionModel implements Serializable {

    @EmbeddedId
    private RolePermissionId id;

    @Column(name = "is_delete")
    private Boolean isDelete;

    // Constructors, getters, and setters
}

@Embeddable
class RolePermissionId implements Serializable {
    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "permission_id")
    private Integer permissionId;

    // Constructors, getters, and setters
}
