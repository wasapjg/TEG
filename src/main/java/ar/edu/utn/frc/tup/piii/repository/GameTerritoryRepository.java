package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.model.entity.GameTerritory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameTerritoryRepository extends JpaRepository<GameTerritory, Long> {
    List<GameTerritory> findByGameId(Long gameId);
}