package com.salesianostriana.dam.tvseries.users

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
        private val repo: UserRepository,
        private val encoder: PasswordEncoder
) {

    fun create(newUser : CreateUserDTO): Optional<User> {
        if (findByUsername(newUser.username).isPresent)
            return Optional.empty()
        return Optional.of(
                with(newUser) {
                    repo.save(User(username, encoder.encode(password), fullName, "USER"))
                }

        )
    }

    fun findByUsername(username : String) = repo.findByUsername(username)

    fun findById(id : UUID) = repo.findById(id)

}