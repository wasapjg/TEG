package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.model.entity.*;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.repository.PlayerRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public  class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepo;

    public PlayerServiceImpl(PlayerRepository playerRepo) {
        this.playerRepo = playerRepo;
    }

    @Override
    @Transactional
    public Player save(Player player) {
        return playerRepo.save(player);
    }

    @Override
    public Optional<Player> findById(Long id) {
        return playerRepo.findById(id);
    }

    @Override
    public List<Player> findAll() {
        return playerRepo.findAll();
    }

    @Override
    public List<Player> findByGame(Game game) {
        return playerRepo.findByGame(game);
    }

    @Override
    public List<Player> findActivePlayersByGame(Game game) {
        return playerRepo.findByGameAndStatus(game, PlayerStatus.WAITING);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        playerRepo.deleteById(id);
    }


    @Transactional
    public Player createHumanPlayer(User user, Game game, int seatOrder) {
        Player player = new Player();
        player.setUser(user);
        player.setGame(game);
        player.setSeatOrder(seatOrder);
        player.setColor(player.getColor());
        player.setStatus(PlayerStatus.WAITING);
        // otros campos necesarios

        return playerRepo.save(player);
    }

    @Override
    @Transactional
    public Player createBotPlayer(BotLevel botLevel, Game game) {
        Player p = new Player();
        p.setBotProfile(BotProfile.create(botLevel, /* podrías pasar estrategia aquí */ null));
        p.setGame(game);
        p.setStatus(PlayerStatus.WAITING);
        // asigna color, seatOrder, etc., según lógica
        return playerRepo.save(p);
    }

    @Override
    public void eliminatePlayer(Long playerId) {

    }

    @Override
    public void activatePlayer(Long playerId) {

    }

    @Override
    public void updateStatus(Long playerId, PlayerStatus status) {

    }

    @Override
    public boolean isEliminated(Long playerId) {
        return false;
    }

    @Override
    public boolean isActive(Long playerId) {
        return false;
    }

    @Override
    public void assignObjective(Long playerId, Objective objective) {

    }

    @Override
    public boolean hasWon(Long playerId, Game game) {
        return false;
    }

    @Override
    public boolean hasAchievedObjective(Long playerId) {
        return false;
    }

    @Override
    public void addArmiesToPlace(Long playerId, int armies) {

    }

    @Override
    public void removeArmiesToPlace(Long playerId, int armies) {

    }

    @Override
    public int getArmiesToPlace(Long playerId) {
        return 0;
    }

    @Override
    public boolean canPerformAction(Long playerId, Game game) {
        return false;
    }

    @Override
    public boolean isPlayerTurn(Long playerId, Game game) {
        return false;
    }

    @Override
    public boolean belongsToGame(Long playerId, Long gameId) {
        return false;
    }

    // Implementa el resto de métodos de PlayerService…
}
