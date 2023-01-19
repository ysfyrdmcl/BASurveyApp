package com.bilgeadam.basurveyapp.repositories;

import com.bilgeadam.basurveyapp.entity.User;
import com.bilgeadam.basurveyapp.repositories.irepository.IUserRepository;
import com.bilgeadam.basurveyapp.repositories.utilities.RepositoryExtension;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRepositoryImpl extends RepositoryExtension<User, Long> {
    private final IUserRepository userRepository;

    public UserRepositoryImpl(IUserRepository userRepository) {
        super(userRepository);
        this.userRepository = userRepository;
    }

    public Optional<User> findByOid(Long userId) {
        return userRepository.findByOid(userId);
    }
}
