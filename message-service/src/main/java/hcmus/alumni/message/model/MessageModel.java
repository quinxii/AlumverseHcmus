package hcmus.alumni.message.model;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;

import hcmus.alumni.message.dto.request.MessageRequestDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "message")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MessageModel implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum MessageType {
        TEXT, IMAGE, FILE, VIDEO, SOUND
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "inbox_id", referencedColumnName = "id", nullable = false)
    @JsonBackReference
    private InboxModel inbox;

    @ManyToOne
    @JoinColumn(name = "sender_id", referencedColumnName = "id", nullable = false)
    private UserModel sender;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @OneToOne
    @JoinColumn(name = "parent_message_id", referencedColumnName = "id")
    private MessageModel parentMessage;

    @CreationTimestamp
    @Column(name = "create_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private Date createAt;

    @UpdateTimestamp
    @Column(name = "update_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updateAt;

    @Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDelete = false;

    public MessageModel(Long id) {
        this.id = id;
    }

    public MessageModel(MessageRequestDto req) {
        this.sender = new UserModel(req.getSenderId());
        this.content = req.getContent();
        this.messageType = req.getMessageType();
        this.parentMessage = req.getParentMessageId() != null ? new MessageModel(req.getParentMessageId()) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MessageModel that = (MessageModel) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
