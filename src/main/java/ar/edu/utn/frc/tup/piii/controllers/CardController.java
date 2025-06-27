package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.card.CardResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.card.CardTradeDto;
import ar.edu.utn.frc.tup.piii.model.Card;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.service.impl.CardServiceImpl;
import ar.edu.utn.frc.tup.piii.service.interfaces.CardService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para la gestión de cartas en el juego TEG
 * Maneja todas las operaciones relacionadas con cartas: obtener, intercambiar, otorgar, etc.
 */
@RestController
@RequestMapping("/api/cards")
@Validated
@CrossOrigin(origins = "*")
public class CardController {

    @Autowired
    private CardService cardService;

    @Autowired
    private CardServiceImpl cardServiceImpl;

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    // ========== ENDPOINTS PARA OBTENER CARTAS ==========


    /**
     * Obtiene todas las cartas de un jugador específico
     */
    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<CardResponseDto>> getPlayerCards(@PathVariable Long playerId) {
        List<CardResponseDto> playerCards = cardService.getPlayerCards(playerId);
        return ResponseEntity.ok(playerCards);
    }

    /**
     * Realiza un intercambio de cartas
     */
    @PostMapping("/trade")
    public ResponseEntity<Integer> tradeCards(@Valid @RequestBody CardTradeDto tradeDto) {
        int tradeValue = cardService.tradeCards(tradeDto);
        return ResponseEntity.ok(tradeValue);
    }

    /**
     * Verifica si un jugador puede intercambiar cartas
     */
    @GetMapping("/player/{playerId}/can-trade")
    public ResponseEntity<Boolean> canPlayerTrade(@PathVariable Long playerId) {
        List<CardResponseDto> playerCards = cardService.getPlayerCards(playerId);
        // Convertir DTOs a Models para validación (esto se podría optimizar)
        // Por ahora asumimos que el service tiene la validación necesaria
        boolean canTrade = playerCards.size() >= 3;
        return ResponseEntity.ok(canTrade);
    }

    /**
     * Verifica si un jugador debe intercambiar cartas obligatoriamente
     */
    @GetMapping("/player/{playerId}/must-trade")
    public ResponseEntity<Boolean> mustPlayerTrade(@PathVariable Long playerId) {
        // Necesitamos crear un Player object para pasar al service
        // Esto se podría mejorar con un método específico en el service
        List<CardResponseDto> playerCards = cardService.getPlayerCards(playerId);
        boolean mustTrade = playerCards.size() >= cardService.getMaxCardsAllowed();
        return ResponseEntity.ok(mustTrade);
    }

    /**
     * Obtiene el número máximo de cartas permitidas
     */
    @GetMapping("/max-cards-allowed")
    public ResponseEntity<Integer> getMaxCardsAllowed() {
        int maxCards = cardService.getMaxCardsAllowed();
        return ResponseEntity.ok(maxCards);
    }

    /**
     * Obtiene el conteo de cartas de un jugador
     */
    @GetMapping("/player/{playerId}/count")
    public ResponseEntity<Integer> getPlayerCardCount(@PathVariable Long playerId) {
        List<CardResponseDto> playerCards = cardService.getPlayerCards(playerId);
        return ResponseEntity.ok(playerCards.size());
    }

}