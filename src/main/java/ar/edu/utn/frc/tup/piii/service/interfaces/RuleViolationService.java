package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.model.entity.RuleViolationReport;
import ar.edu.utn.frc.tup.piii.model.entity.Vote;
import ar.edu.utn.frc.tup.piii.model.entity.Player;
import ar.edu.utn.frc.tup.piii.model.entity.Game;
import java.util.List;
import java.util.Optional;

public interface RuleViolationService {

    // CRUD básico para reportes
    RuleViolationReport save(RuleViolationReport report);
    Optional<RuleViolationReport> findById(Long id);
    List<RuleViolationReport> findAll();
    List<RuleViolationReport> findByGame(Game game);
    void deleteById(Long id);

    // Gestión de reportes
    RuleViolationReport reportViolation(Player reporter, Player reported, Game game, String reason);
    void resolveReport(Long reportId);
    List<RuleViolationReport> getPendingReports(Game game);

    // Sistema de votación
    Vote castVote(Player voter, RuleViolationReport report, boolean approve);
    boolean hasVoted(Player voter, RuleViolationReport report);
    int getVoteCount(RuleViolationReport report, boolean approve);
    boolean isVotingComplete(RuleViolationReport report);
    void processVotingResult(RuleViolationReport report);

    // Validaciones
    boolean canReportPlayer(Player reporter, Player reported, Game game);
    boolean canVote(Player voter, RuleViolationReport report);

    // Acciones disciplinarias
    void warnPlayer(Player player);
    void kickPlayer(Player player, Game game);
    void temporaryBan(Player player, int minutes);
}
