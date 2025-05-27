package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.model.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByGameIdOrderBySentAtAsc(Long gameId);
}