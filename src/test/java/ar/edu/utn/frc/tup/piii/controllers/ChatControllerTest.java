package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.chat.ChatMessageDto;
import ar.edu.utn.frc.tup.piii.dtos.chat.ChatMessageResponseDto;
import ar.edu.utn.frc.tup.piii.service.interfaces.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    private ChatMessageDto chatMessageDto;
    private ChatMessageResponseDto chatMessageResponseDto;
    private String gameCode;

    @BeforeEach
    void setUp() {
        gameCode = "GAME123";

        chatMessageDto = new ChatMessageDto();
        chatMessageDto.setSenderId(1L);
        chatMessageDto.setContent("Test message");
        chatMessageDto.setGameId(1L);

        chatMessageResponseDto = new ChatMessageResponseDto();
        chatMessageResponseDto.setId(1L);
        chatMessageResponseDto.setSenderName("Test Player");
        chatMessageResponseDto.setContent("Test message");
        chatMessageResponseDto.setSentAt(LocalDateTime.now());
        chatMessageResponseDto.setIsSystemMessage(false);
    }

    @Test
    void sendMessage_Success() throws Exception {
        // Given
        when(chatService.sendMessage(eq(gameCode), any(ChatMessageDto.class)))
                .thenReturn(chatMessageResponseDto);

        // When & Then
        mockMvc.perform(post("/api/games/{gameCode}/chat", gameCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatMessageDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Test message"))
                .andExpect(jsonPath("$.senderName").value("Test Player"));
    }

    @Test
    void sendMessage_InvalidGame_ReturnsBadRequest() throws Exception {
        // Given
        when(chatService.sendMessage(eq(gameCode), any(ChatMessageDto.class)))
                .thenThrow(new IllegalArgumentException("Game not found"));

        // When & Then
        mockMvc.perform(post("/api/games/{gameCode}/chat", gameCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatMessageDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getNewMessages_WithoutSince_Success() throws Exception {
        // Given
        List<ChatMessageResponseDto> messages = Arrays.asList(chatMessageResponseDto);
        when(chatService.getNewMessages(gameCode, null))
                .thenReturn(messages);

        // When & Then
        mockMvc.perform(get("/api/games/{gameCode}/chat/new", gameCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("1"));
    }

    @Test
    void getNewMessages_WithSince_Success() throws Exception {
        // Given
        String sinceId = "5";
        List<ChatMessageResponseDto> messages = Arrays.asList(chatMessageResponseDto);
        when(chatService.getNewMessages(gameCode, sinceId))
                .thenReturn(messages);

        // When & Then
        mockMvc.perform(get("/api/games/{gameCode}/chat/new", gameCode)
                        .param("since", sinceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("1"));
    }

    @Test
    void getNewMessages_EmptyResult_Success() throws Exception {
        // Given
        when(chatService.getNewMessages(gameCode, null))
                .thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/games/{gameCode}/chat/new", gameCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getNewMessages_InvalidGameCode_ReturnsBadRequest() throws Exception {
        // Given
        when(chatService.getNewMessages(gameCode, null))
                .thenThrow(new IllegalArgumentException("Game not found"));

        // When & Then
        mockMvc.perform(get("/api/games/{gameCode}/chat/new", gameCode))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllMessages_Success() throws Exception {
        // Given
        List<ChatMessageResponseDto> messages = Arrays.asList(chatMessageResponseDto);
        when(chatService.getNewMessages(gameCode, null))
                .thenReturn(messages);

        // When & Then
        mockMvc.perform(get("/api/games/{gameCode}/chat", gameCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("1"));
    }
}
