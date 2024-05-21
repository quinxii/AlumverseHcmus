package hcmus.alumni.authservice.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "[password_reset]")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PasswordResetModel {
	@Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password_status", nullable = false)
    private Boolean passwordStatus;

    @CreationTimestamp
	@Column(name = "reset_date")
    private Date resetDate;

    public PasswordResetModel(String id) {
		this.id = id;
	}
}