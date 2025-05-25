package ar.edu.utn.frc.tup.piii.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "player_hands") public class PlayerHand { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @PrePersist
    @PreUpdate
    protected void validateQuantity() {
        if (quantity != null && quantity < 0) {
            quantity = 0;
        }
    }

}

