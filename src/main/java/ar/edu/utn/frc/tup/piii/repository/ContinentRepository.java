package ar.edu.utn.frc.tup.piii.repository;

import ar.edu.utn.frc.tup.piii.model.entity.Continent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContinentRepository extends JpaRepository<Continent, Long> {
}