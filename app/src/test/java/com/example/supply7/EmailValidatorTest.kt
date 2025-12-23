package com.example.supply7

import com.example.supply7.utils.EmailValidator
import org.junit.Test
import org.junit.Assert.*

class EmailValidatorTest {
    @Test
    fun emailValidator_CorrectDomain_ReturnsTrue() {
        assertTrue(EmailValidator.isValid("student@std.yeditepe.edu.tr"))
    }

    @Test
    fun emailValidator_IncorrectDomain_ReturnsFalse() {
        assertFalse(EmailValidator.isValid("student@gmail.com"))
        assertFalse(EmailValidator.isValid("student@yeditepe.edu.tr"))
        assertFalse(EmailValidator.isValid("random"))
    }

    @Test
    fun emailValidator_Empty_ReturnsFalse() {
        assertFalse(EmailValidator.isValid(""))
    }
}
