package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.model.entity.Country;
import ar.edu.utn.frc.tup.piii.model.entity.Continent;
import ar.edu.utn.frc.tup.piii.model.entity.Player;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CountryService {

    // CRUD básico
    Country save(Country country);
    Optional<Country> findById(Long id);
    Optional<Country> findByName(String name);
    List<Country> findAll();
    List<Country> findByContinent(Continent continent);
    List<Country> findByOwner(Player owner);
    void deleteById(Long id);

    // Gestión de propietario
    void assignOwner(Long countryId, Player owner);
    void changeOwner(Long countryId, Player newOwner);
    boolean isOwnedBy(Long countryId, Player player);

    // Gestión de ejércitos
    void addArmies(Long countryId, int armies);
    void removeArmies(Long countryId, int armies);
    void setArmies(Long countryId, int armies);
    int getArmies(Long countryId);

    // Relaciones territoriales
    Set<Country> getNeighbors(Long countryId);
    boolean areNeighbors(Long countryId1, Long countryId2);
    boolean canAttackFrom(Long fromCountryId, Long toCountryId);
    List<Country> getAttackableCountries(Long countryId);
    List<Country> getDefensibleCountries(Long countryId);

    // Validaciones
    boolean canAttack(Long countryId, int attackingArmies);
    boolean canDefend(Long countryId);
    boolean hasMinimumArmies(Long countryId, int minimum);

    // Utilidades del mapa
    List<Country> findCountriesInContinent(String continentName);
    List<Country> findBorderCountries(Player player);
    List<Country> findInteriorCountries(Player player);
}

