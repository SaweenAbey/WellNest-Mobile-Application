package com.example.wellnest_mobile_application.database.daos

import androidx.room.*
import com.example.wellnest_mobile_application.database.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?
    
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun getUserByCredentials(email: String, password: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Query("UPDATE users SET isLoggedIn = 0, sessionActive = 0")
    suspend fun logoutAllUsers()
    
    @Query("UPDATE users SET isLoggedIn = 1, sessionActive = 1 WHERE id = :userId")
    suspend fun loginUser(userId: Int)
    
    @Query("SELECT COUNT(*) FROM users WHERE isLoggedIn = 1")
    suspend fun isLoggedIn(): Int
    
    @Query("SELECT COUNT(*) FROM users WHERE sessionActive = 1")
    suspend fun isSessionActive(): Int
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Int)
    
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}
