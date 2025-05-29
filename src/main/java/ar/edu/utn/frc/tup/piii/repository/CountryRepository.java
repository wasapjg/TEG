package ar.edu.utn.frc.tup.piii.repository;


import ar.edu.utn.frc.tup.piii.model.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
}