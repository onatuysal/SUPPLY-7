package com.example.supply7.utils

object EmailValidator {
    private const val VALID_DOMAIN = "@std.yeditepe.edu.tr"

    fun isValid(email: String): Boolean {
        return email.isNotEmpty() && email.endsWith(VALID_DOMAIN)
    }
}
