package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.model.entity.Card;
import ar.edu.utn.frc.tup.piii.model.entity.Deck;
import java.util.List;
import java.util.Optional;

public interface DeckService {

    // CRUD básico
    Deck save(Deck deck);
    Optional<Deck> findById(Long id);
    List<Deck> findAll();
    void deleteById(Long id);
    boolean existsById(Long id);

    // Métodos de negocio específicos del TEG
    Deck createNewDeck(List<Card> cards);
    Deck createStandardTegDeck();
    Card drawCard(Long deckId);
    void discardCard(Long deckId, Card card);
    void discardCards(Long deckId, List<Card> cards);
    int getRemainingCards(Long deckId);
    int getTotalCards(Long deckId);
    void shuffleDeck(Long deckId);
    void reshuffleDiscardPile(Long deckId);
    boolean isEmpty(Long deckId);
    boolean canDraw(Long deckId);

    // Métodos para el juego
    List<Card> drawMultipleCards(Long deckId, int count);
    void resetDeck(Long deckId, List<Card> newCards);
    Deck cloneDeck(Long deckId);
}
