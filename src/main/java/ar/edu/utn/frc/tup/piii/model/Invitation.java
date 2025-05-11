package ar.edu.utn.frc.tup.piii.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "invitations") public class Invitation { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    public Invitation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }
    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

}

