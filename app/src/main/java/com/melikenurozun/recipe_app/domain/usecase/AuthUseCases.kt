package com.melikenurozun.recipe_app.domain.usecase

import com.melikenurozun.recipe_app.domain.repository.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        return repository.signIn(email, password)
    }
}

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, username: String? = null): Result<Unit> {
        return authRepository.signUp(email, password, username)
    }
}

class SignOutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() {
        repository.signOut()
    }
}
