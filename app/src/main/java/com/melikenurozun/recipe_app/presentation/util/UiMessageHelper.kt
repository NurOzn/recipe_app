package com.melikenurozun.recipe_app.presentation.util

object UiMessageHelper {
    fun getMessage(t: Throwable?): String {
        val msg = t?.message ?: return "An unknown error occurred."
        
        return when {
            msg.contains("Unable to resolve host", ignoreCase = true) || 
            msg.contains("No address associated with hostname", ignoreCase = true) -> "No internet connection. Please check your network settings."
            
            msg.contains("timeout", ignoreCase = true) || 
            msg.contains("Timed out", ignoreCase = true) -> "Connection timed out. Please try again."
            
            msg.contains("invalid_grant", ignoreCase = true) ||
            msg.contains("Invalid login credentials", ignoreCase = true) -> "Invalid email or password."
            
            msg.contains("User not found", ignoreCase = true) -> "User not found."
            
            msg.contains("Email not confirmed", ignoreCase = true) -> "Please confirm your email address before logging in."
            
            msg.contains("User already registered", ignoreCase = true) ||
            msg.contains("already taken", ignoreCase = true) -> "Email or username is already taken."
            
            msg.contains("duplicate key value", ignoreCase = true) -> "This action has already been performed."
            
            msg.contains("foreign key violation", ignoreCase = true) -> "Related data not found."
            
            else -> "An error occurred: $msg"
        }
    }
}
