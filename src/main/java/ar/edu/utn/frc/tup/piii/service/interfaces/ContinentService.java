package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.model.entity.Continent;
import ar.edu.utn.frc.tup.piii.model.entity.Country;
import ar.edu.utn.frc.tup.piii.model.entity.Player;
import java.util.List;
import java.util.Optional;

public interface ContinentService {

    // CRUD básico
    Continent save(Continent continent);
    Optional<Continent> findById(Long id);
    Optional<Continent> findByName(String name);
    List<Continent> findAll();
    void deleteById(Long id);

    // Control de continente
    boolean isControlledBy(Long continentId, Player player);
    Player getController(Long continentId);
    List<Continent> getContinentsControlledBy(Player player);
    int getBonusArmies(Long continentId);

    // Estadísticas
    int getTotalCountries(Long continentId);
    int getCountriesOwnedBy(Long continentId, Player player);
    double getControlPercentage(Long continentId, Player player);

    // Utilidades
    List<Country> getCountriesInContinent(Long continentId);
    boolean isCountryInContinent(Long countryId, Long continentId);
    List<Player> getPlayersInContinent(Long continentId);
}