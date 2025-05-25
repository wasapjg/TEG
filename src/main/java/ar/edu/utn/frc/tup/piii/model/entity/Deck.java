package ar.edu.utn.frc.tup.piii.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "decks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"game"})
@ToString(exclude = {"game"})
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderColumn(name = "draw_order")
    @Builder.Default
    private List<Card> drawPile = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "discard_deck_id")
    @OrderColumn(name = "discard_order")
    @Builder.Default
    private List<Card> discardPile = new ArrayList<>();

    @OneToOne(mappedBy = "deck")
    private Game game;

    // Constructor personalizado para inicialización con cartas
    public Deck(List<Card> initialCards) {
        this.drawPile = new ArrayList<>(initialCards);
        this.discardPile = new ArrayList<>();
        // Shuffle the initial deck
        Collections.shuffle(this.drawPile);
    }

    // Métodos de negocio
    public Card draw() {
        if (drawPile.isEmpty()) {
            reshuffleDiscardPile();
        }

        if (drawPile.isEmpty()) {
            return null; // No hay más cartas
        }

        return drawPile.remove(drawPile.size() - 1);
    }

    public void discard(List<Card> cards) {
        if (cards != null && !cards.isEmpty()) {
            discardPile.addAll(cards);
        }
    }

    public void discard(Card card) {
        if (card != null) {
            discardPile.add(card);
        }
    }

    public int remaining() {
        return drawPile.size();
    }

    public int totalCards() {
        return drawPile.size() + discardPile.size();
    }

    private void reshuffleDiscardPile() {
        if (!discardPile.isEmpty()) {
            drawPile.addAll(discardPile);
            discardPile.clear();
            Collections.shuffle(drawPile);
        }
    }

}
