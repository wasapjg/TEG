package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.model.entity.Game;
import ar.edu.utn.frc.tup.piii.model.entity.Player;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByGame(Game game);
    List<Player> findByGameAndStatus(Game game, PlayerStatus status);

}
