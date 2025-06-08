package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.game.AttackDto;
import ar.edu.utn.frc.tup.piii.dtos.game.CombatResultDto;
import ar.edu.utn.frc.tup.piii.service.interfaces.CombatService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/combat")
public class CombatTestController {

    @Autowired
    private CombatService combatService;

    @PostMapping("/attack")
    public ResponseEntity<CombatResultDto> testAttack(@RequestBody AttackRequest request) {
        try {
            AttackDto attackDto = AttackDto.builder()
                    .playerId(request.getPlayerId())
                    .attackerCountryId(request.getAttackerCountryId())
                    .defenderCountryId(request.getDefenderCountryId())
                    .attackingArmies(request.getAttackingArmies())
                    .build();

            CombatResultDto result = combatService.performCombat(request.getGameCode(), attackDto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Data
    public static class AttackRequest {
        private String gameCode;
        private Long playerId;
        private Long attackerCountryId;
        private Long defenderCountryId;
        private Integer attackingArmies;
    }
}