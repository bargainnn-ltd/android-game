package com.spicynights.games.ui.onboarding

import java.time.DateTimeException
import java.time.LocalDate
import java.time.Period

sealed class DobValidationResult {
    data object Incomplete : DobValidationResult()
    data object InvalidDate : DobValidationResult()
    data object Under18 : DobValidationResult()
    data object Valid : DobValidationResult()
}

fun validateDob(mm: String, dd: String, yyyy: String): DobValidationResult {
    val m = mm.trim()
    val d = dd.trim()
    val y = yyyy.trim()
    if (m.isEmpty() || d.isEmpty() || y.isEmpty()) return DobValidationResult.Incomplete
    if (m.length > 2 || d.length > 2 || y.length != 4) return DobValidationResult.Incomplete
    val month = m.toIntOrNull() ?: return DobValidationResult.InvalidDate
    val day = d.toIntOrNull() ?: return DobValidationResult.InvalidDate
    val year = y.toIntOrNull() ?: return DobValidationResult.InvalidDate
    if (month !in 1..12 || day !in 1..31 || year < 1900 || year > 2100) return DobValidationResult.InvalidDate

    val birth = try {
        LocalDate.of(year, month, day)
    } catch (_: DateTimeException) {
        return DobValidationResult.InvalidDate
    }

    val today = LocalDate.now()
    if (birth.isAfter(today)) return DobValidationResult.InvalidDate

    val age = Period.between(birth, today).years
    if (age < 18) return DobValidationResult.Under18
    return DobValidationResult.Valid
}
