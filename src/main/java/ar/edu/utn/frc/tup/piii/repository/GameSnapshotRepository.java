package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.dtos.game.GameSnapshotDto;
import ar.edu.utn.frc.tup.piii.model.entity.Game;
import ar.edu.utn.frc.tup.piii.model.entity.GameSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameSnapshotRepository extends JpaRepository<GameSnapshot, Long> {

    // Devuelve todos los snapshots de un juego, ordenados por turno
    List<GameSnapshot> findByGameOrderByTurnNumberAsc(Game game);

    // Devuelve el último snapshot de un juego
    Optional<GameSnapshot> findTopByGameOrderByTurnNumberDesc(Game game);

    // Devuelve todos los snapshots de un juego, pero mapeados a DTO (si querés usar proyecciones)
    //TODO: la query a la DB
    @Query("SELECT new com.tu.paquete.dto.GameSnapshotDTO(s.id, s.turnNumber, s.createdAt, s.createdBySystem)"  +
            "FROM GameSnapshot s WHERE s.game.id = :gameId ORDER BY s.turnNumber ASC")
    List<GameSnapshotDto> findDtoByGameId(@Param("gameId") Long gameId);
}
