package hcmus.alumni.authservice.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "email_reset_code")
public class EmailResetCodeModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "email", nullable = false, length = 200)
    private String email;

    @Column(name = "reset_code", nullable = false, length = 8)
    private String resetCode;

    @Column(name = "created_at", nullable = false)
    private String createdAt;

    // Constructors, getters, and setters
    // Constructor
    public EmailResetCodeModel() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getResetCode() {
        return resetCode;
    }

    public void setResetCode(String resetCode) {
        this.resetCode = resetCode;
    }

    public String getCreateAt() {
        return createdAt;
    }

    public void setCreateAt(String currentTime) {
        this.createdAt = currentTime;
    }
}

