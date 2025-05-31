package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.entities.UserEntity;
import ar.edu.utn.frc.tup.piii.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    //Method to map UserEntity to UserDto
    public User toModel(UserEntity entity){
        if (entity == null) {
            return null;
        }
        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .passwordHash(entity.getPasswordHash())
                .email(entity.getEmail())
                .avatarUrl(entity.getAvatarUrl())
                .lastLogin(entity.getLastLogin())
                .build();
    }

    public UserEntity toEntity(User u) {
        if (u == null) {
            return null;
        }
        UserEntity entity = new UserEntity();
        entity.setId(u.getId());
        entity.setUsername(u.getUsername());
        entity.setPasswordHash(u.getPasswordHash());
        entity.setEmail(u.getEmail());
        entity.setAvatarUrl(u.getAvatarUrl());
        entity.setLastLogin(u.getLastLogin());
        return entity;
    }
}
