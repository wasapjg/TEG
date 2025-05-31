package ar.edu.utn.frc.tup.piii.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String avatarUrl;
    private boolean isActive;
    private String createdAt;
    private String updatedAt;
}
