package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.model.entity.Card;
import ar.edu.utn.frc.tup.piii.model.enums.CardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByOwnerId(Long playerId);
    List<Card> findByGameIdAndIsInDeckTrue(Long gameId);
    List<Card> findByType(CardType type);
}