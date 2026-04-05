package com.application.eatbts.ui.onboarding

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class AgeDobValidationTest {

    @Test
    fun incomplete_when_empty() {
        assertEquals(DobValidationResult.Incomplete, validateDob("", "", ""))
        assertEquals(DobValidationResult.Incomplete, validateDob("01", "", "2000"))
    }

    @Test
    fun invalidDate_feb30() {
        assertEquals(DobValidationResult.InvalidDate, validateDob("02", "30", "2000"))
    }

    @Test
    fun invalidDate_feb29_nonLeap() {
        assertEquals(DobValidationResult.InvalidDate, validateDob("02", "29", "2021"))
    }

    @Test
    fun validDate_feb29_leapYear() {
        assertEquals(DobValidationResult.Valid, validateDob("02", "29", "2000"))
    }

    @Test
    fun under18_recent() {
        val birth = LocalDate.now().minusYears(17).minusDays(1)
        assertEquals(
            DobValidationResult.Under18,
            validateDob(
                birth.monthValue.toString().padStart(2, '0'),
                birth.dayOfMonth.toString().padStart(2, '0'),
                birth.year.toString(),
            ),
        )
    }

    @Test
    fun exactly18_today() {
        val birth = LocalDate.now().minusYears(18)
        val r = validateDob(
            birth.monthValue.toString().padStart(2, '0'),
            birth.dayOfMonth.toString().padStart(2, '0'),
            birth.year.toString(),
        )
        assertEquals(DobValidationResult.Valid, r)
    }

    @Test
    fun valid_100YearsOld() {
        val birth = LocalDate.now().minusYears(100)
        assertEquals(
            DobValidationResult.Valid,
            validateDob(
                birth.monthValue.toString().padStart(2, '0'),
                birth.dayOfMonth.toString().padStart(2, '0'),
                birth.year.toString(),
            ),
        )
    }

    @Test
    fun futureDate_invalid() {
        val future = LocalDate.now().plusDays(1)
        assertEquals(
            DobValidationResult.InvalidDate,
            validateDob(
                future.monthValue.toString().padStart(2, '0'),
                future.dayOfMonth.toString().padStart(2, '0'),
                future.year.toString(),
            ),
        )
    }
}
