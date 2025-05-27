package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.model.entity.GameEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameEventRepository extends JpaRepository<GameEvent, Long> {
    List<GameEvent> findByGameIdOrderByTimestampAsc(Long gameId);
}