package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.model.entity.BotProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BotProfileRepository extends JpaRepository<BotProfile, Long> {
}
