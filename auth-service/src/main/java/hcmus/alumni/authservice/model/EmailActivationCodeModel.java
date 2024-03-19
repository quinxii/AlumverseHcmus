package hcmus.alumni.authservice.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "email_activation_code")
public class EmailActivationCodeModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "email", nullable = false, length = 200)
    private String email;

    @Column(name = "activation_code", nullable = false, length = 8)
    private String activationCode;

    @Column(name = "create_at", nullable = false)
    private String createAt;

    // Constructors, getters, and setters
    // Constructor
    public EmailActivationCodeModel() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String currentTime) {
        this.createAt = currentTime;
    }
}


