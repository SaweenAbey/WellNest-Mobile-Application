package com.example.wellnest_mobile_application.database.repositories

import com.example.wellnest_mobile_application.database.WellnestDatabase
import com.example.wellnest_mobile_application.database.daos.UserDao
import com.example.wellnest_mobile_application.database.entities.UserEntity
import com.example.wellnest_mobile_application.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(private val database: WellnestDatabase) {
    
    private val userDao: UserDao = database.userDao()
    
    suspend fun saveUser(user: User): Boolean {
        return try {
            val userEntity = UserEntity(
                fullName = user.fullName,
                email = user.email,
                password = user.password,
                registrationDate = user.registrationDate,
                isLoggedIn = true,
                sessionActive = true
            )
            userDao.insertUser(userEntity)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getUser(): User? {
        val userEntity = userDao.getCurrentUser()
        return userEntity?.let {
            User(
                fullName = it.fullName,
                email = it.email,
                password = it.password,
                registrationDate = it.registrationDate
            )
        }
    }
    
    suspend fun getUserByCredentials(email: String, password: String): User? {
        val userEntity = userDao.getUserByCredentials(email, password)
        return userEntity?.let {
            User(
                fullName = it.fullName,
                email = it.email,
                password = it.password,
                registrationDate = it.registrationDate
            )
        }
    }
    
    suspend fun logout() {
        userDao.logoutAllUsers()
    }
    
    suspend fun isLoggedIn(): Boolean {
        return userDao.isLoggedIn() > 0
    }
    
    suspend fun startSession() {
        val user = userDao.getCurrentUser()
        user?.let {
            userDao.loginUser(it.id)
        }
    }
    
    suspend fun isSessionActive(): Boolean {
        return userDao.isSessionActive() > 0
    }
    
    suspend fun clearAllData() {
        userDao.deleteAllUsers()
    }
}
