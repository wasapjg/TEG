package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.user.UserUpdateDto;
import ar.edu.utn.frc.tup.piii.entities.UserEntity;
import ar.edu.utn.frc.tup.piii.exceptions.EmailAlreadyExistsException;
import ar.edu.utn.frc.tup.piii.exceptions.UserAlreadyExistsException;
import ar.edu.utn.frc.tup.piii.exceptions.UserNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.UserMapper;
import ar.edu.utn.frc.tup.piii.model.User;
import ar.edu.utn.frc.tup.piii.repository.UserRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    @Qualifier("mergerMapper")
    private ModelMapper mergerMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /*
    * retrieve a user by username
    * or
    */
    @Override
    public User getUserByUserName(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toModel)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

//    @Override
//    public User getUserByEmail(String email) {
//        return userRepository.findByEmail(email)
//                .map(userMapper::toModel)
//                .orElseThrow(() -> new EmailNotFoundException("Email not found with username: " + email));
//    }

    @Override
    public Boolean existsByUserName(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toModel)
                .toList();
    }

    @Override
    public void save(User user) {
        UserEntity userEntity = userMapper.toEntity(user);
        userRepository.save(userEntity);
    }

    @Override
    public User getUserByUserNameAndPasswordHash(String userName, String password) {
        Optional<UserEntity> userEntity = userRepository.findByUsernameAndPasswordHash(userName, password);
        if(userEntity.get().getUsername().describeConstable().isPresent()){
            return userMapper.toModel(userEntity.get());
        }else {
            throw new EntityNotFoundException("Username or password invalid!");
        }
    }

    @Override
    public User getUserByEmailAndPasswordHash(String email, String password) {
        Optional<UserEntity> userEntity = userRepository.findByEmailAndPasswordHash(email, password);
        if(userEntity.isPresent()) {
            return userMapper.toModel(userEntity.get());
        }else{
            throw new EntityNotFoundException("Email or password invalid!");
        }
    }

    @Override
    public User getUserById(Long id){
        return userRepository.findById(id)
                .map(userMapper::toModel)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserUpdateDto userUpdateDto) {
        // Fetch the existing user entity
        UserEntity existingUserEntity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        // Validate unique constraints before updating
        if (userUpdateDto.getUsername() != null &&
                !userUpdateDto.getUsername().equals(existingUserEntity.getUsername()) &&
                userRepository.existsByUsername(userUpdateDto.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + userUpdateDto.getUsername());
        }

        if (userUpdateDto.getEmail() != null &&
                !userUpdateDto.getEmail().equals(existingUserEntity.getEmail()) &&
                userRepository.existsByEmail(userUpdateDto.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + userUpdateDto.getEmail());
        }

        // Apply updates using mergerMapper (ignores null fields)
        mergerMapper.map(userUpdateDto, existingUserEntity);

        // Hash password if provided
        if (userUpdateDto.getPassword() != null && !userUpdateDto.getPassword().trim().isEmpty()) {
            existingUserEntity.setPasswordHash(passwordEncoder.encode(userUpdateDto.getPassword()));
        }

        // Save the updated entity
        UserEntity updatedEntity = userRepository.save(existingUserEntity);

        // Return the updated user model
        return userMapper.toModel(updatedEntity);
    }
}
