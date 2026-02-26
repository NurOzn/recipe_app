package com.melikenurozun.recipe_app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import io.github.jan.supabase.annotations.SupabaseInternal
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @OptIn(SupabaseInternal::class)
    @Provides
    @Singleton
    fun provideSupabaseClient(@ApplicationContext context: Context): SupabaseClient {
        // Using standard SharedPreferences to avoid device compatibility issues with EncryptedSharedPreferences
        val sharedPreferences = context.getSharedPreferences("supabase_session_v2", Context.MODE_PRIVATE)
        
        val jsonParser = Json { 
            ignoreUnknownKeys = true 
            encodeDefaults = true
        }

        return createSupabaseClient(
            supabaseUrl = "https://jidkdcxnyeyzmxtqripc.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImppZGtkY3hueWV5em14dHFyaXBjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjkxOTE4ODgsImV4cCI6MjA4NDc2Nzg4OH0.7t46aZFGUkuksvc0P9vy2YUqUQDHGm8Yr3KEmZt8d-Q"
        ) {
            install(Auth) {
                sessionManager = object : SessionManager {
                    override suspend fun saveSession(session: UserSession) {
                        val json = jsonParser.encodeToString(session)
                        sharedPreferences.edit().putString("session", json).apply()
                    }

                    override suspend fun loadSession(): UserSession? {
                        val json = sharedPreferences.getString("session", null) ?: return null
                        return try {
                           jsonParser.decodeFromString(json)
                        } catch(e: Exception) {
                            e.printStackTrace()
                            // Critical Fix: Clear corrupted session data
                            sharedPreferences.edit().remove("session").apply()
                            null
                        }
                    }

                    override suspend fun deleteSession() {
                        sharedPreferences.edit().remove("session").apply()
                    }
                }
            }
            httpConfig {
                install(io.ktor.client.plugins.HttpTimeout) {
                    requestTimeoutMillis = 60000
                    connectTimeoutMillis = 60000
                    socketTimeoutMillis = 60000
                }
            }
            install(Postgrest)
            install(Storage)
            defaultSerializer = KotlinXSerializer(jsonParser)
        }
    }
}
