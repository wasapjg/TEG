package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.model.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByGameCode(String gameCode);
}
