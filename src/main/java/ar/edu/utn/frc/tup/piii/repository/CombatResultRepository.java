package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.model.entity.CombatResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CombatResultRepository extends JpaRepository<CombatResult, Long> {
    List<CombatResult> findByGameId(Long gameId);
}