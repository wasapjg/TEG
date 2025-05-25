package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.dtos.card.CardResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.card.CardTradeDto;
import ar.edu.utn.frc.tup.piii.model.entity.Card;
import ar.edu.utn.frc.tup.piii.model.entity.Game;
import ar.edu.utn.frc.tup.piii.model.entity.Player;
import ar.edu.utn.frc.tup.piii.model.enums.CardType;
import java.util.List;
import java.util.Optional;

public interface CardService {

    // CRUD básico
    Card save(Card card);
    Optional<Card> findById(Long id);
    List<Card> findAll();
    List<Card> findByGame(Game game);
    List<Card> findByPlayer(Player player);
    void deleteById(Long id);

    // Gestión de cartas del jugador
    List<CardResponseDto> getPlayerCards(Long playerId);
    Card drawCard(Game game, Player player);
    List<Card> drawCards(Game game, Player player, int count);
    void giveCardToPlayer(Card card, Player player);

    // Intercambio de cartas
    int tradeCards(CardTradeDto tradeDto);
    boolean canTradeCards(List<Card> cards);
    int calculateTradeValue(List<Card> cards, int tradeCount);
    boolean isValidCardCombination(List<Card> cards);

    // Validaciones
    boolean hasPlayerMaxCards(Player player);
    boolean mustTradeCards(Player player);
    int getMaxCardsAllowed();

    // Tipos de cartas
    List<Card> getCardsByType(CardType type);
    int countCardsByType(Player player, CardType type);
    boolean hasThreeOfSameType(List<Card> cards);
    boolean hasOneOfEachType(List<Card> cards);

    // Utilidades
    void shufflePlayerHand(Player player);
    Card getRandomCard(Game game);
    List<Card> getAllAvailableCards(Game game);
}