package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.chat.ChatMessageDto;
import ar.edu.utn.frc.tup.piii.dtos.chat.ChatMessageResponseDto;
import ar.edu.utn.frc.tup.piii.service.interfaces.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * Envía un mensaje de chat a un juego específico.
     *
     * @param gameCode Código único del juego
     * @param messageDto Datos del mensaje a enviar
     * @return ResponseEntity con el mensaje enviado y código HTTP 201 (CREATED)
     */
    @PostMapping("/{gameCode}/chat")
    public ResponseEntity<ChatMessageResponseDto> sendMessage(
            @PathVariable String gameCode,
            @Valid @RequestBody ChatMessageDto messageDto) {

        try {
            ChatMessageResponseDto response = chatService.sendMessage(gameCode, messageDto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Obtiene los mensajes nuevos de chat de un juego específico.
     *
     * Este endpoint soporta polling incremental:
     * - Sin parámetro 'since': devuelve todos los mensajes del juego
     * - Con parámetro 'since': devuelve solo mensajes posteriores al ID especificado
     *
     * @param gameCode Código único del juego
     * @param since ID del último mensaje conocido (opcional)
     * @return ResponseEntity con la lista de mensajes y código HTTP 200 (OK)
     */
    @GetMapping("/{gameCode}/chat/new")
    public ResponseEntity<List<ChatMessageResponseDto>> getNewMessages(
            @PathVariable String gameCode,
            @RequestParam(value = "since", required = false) String since) {

        try {
            List<ChatMessageResponseDto> messages = chatService.getNewMessages(gameCode, since);
            return new ResponseEntity<>(messages, HttpStatus.OK);
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint adicional para obtener todos los mensajes de un juego
     * (útil para cargar el historial completo)
     *
     * @param gameCode Código único del juego
     * @return ResponseEntity con todos los mensajes del juego
     */
    @GetMapping("/{gameCode}/chat")
    public ResponseEntity<List<ChatMessageResponseDto>> getAllMessages(
            @PathVariable String gameCode) {

        try {
            List<ChatMessageResponseDto> messages = chatService.getNewMessages(gameCode, null);
            return new ResponseEntity<>(messages, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}