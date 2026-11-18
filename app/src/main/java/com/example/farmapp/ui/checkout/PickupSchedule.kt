package com.example.farmapp.ui.checkout

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * HU04 - Calendario de recojo de demostración.
 *
 * Considera lunes a viernes como días hábiles.
 * No contempla feriados oficiales.
 */
data class PickupSchedule(
    val readyAt: LocalDateTime,
    val deadlineAt: LocalDateTime
)

/**
 * Avanza la cantidad indicada de días hábiles.
 */
fun addBusinessDays(
    start: LocalDate,
    days: Int
): LocalDate {

    require(days >= 0)

    var result = start
    var counted = 0

    while (counted < days) {

        result = result.plusDays(1)

        if (
            result.dayOfWeek != DayOfWeek.SATURDAY &&
            result.dayOfWeek != DayOfWeek.SUNDAY
        ) {
            counted++
        }
    }

    return result
}

/**
 * Política de demostración:
 *
 * - Disponible el siguiente día hábil a las 08:00.
 * - Plazo máximo de tres días hábiles posteriores,
 *   hasta las 21:00.
 */
fun estimatePickupSchedule(
    today: LocalDate
): PickupSchedule {

    val readyDate = addBusinessDays(today, 1)
    val deadlineDate = addBusinessDays(readyDate, 3)

    return PickupSchedule(
        readyAt = readyDate.atTime(LocalTime.of(8, 0)),
        deadlineAt = deadlineDate.atTime(LocalTime.of(21, 0))
    )
}

fun formatPickupDate(
    dateTime: LocalDateTime
): String {

    val formatter = DateTimeFormatter.ofPattern(
        "EEEE d 'de' MMMM 'a las' HH:mm",
        Locale.forLanguageTag("es-PE")
    )

    return dateTime.format(formatter)
}