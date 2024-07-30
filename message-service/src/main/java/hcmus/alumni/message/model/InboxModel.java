package hcmus.alumni.message.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inbox")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InboxModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", columnDefinition = "TINYTEXT")
    private String name;

    @Column(name = "is_group", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isGroup = false;

    @CreationTimestamp
    @Column(name = "create_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private Date createAt;

    @UpdateTimestamp
    @Column(name = "update_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updateAt;

    @Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDelete = false;

    @OneToMany(mappedBy = "inbox", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<InboxMemberModel> members = new HashSet<>();

    @Transient
    @JsonManagedReference
    private MessageModel latestMessage;

    @Transient
    private boolean hasRead;

    public InboxModel(Long id) {
        this.id = id;
    }

    public void setMembers(Set<InboxMemberModel> members) {
        if (members != null) {
            for (var member : members) {
                member.setInbox(this);
            }

            this.members.clear();
            this.members.addAll(members);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        InboxModel that = (InboxModel) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }
}
