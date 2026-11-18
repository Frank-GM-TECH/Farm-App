package com.example.farmapp.ui.checkout

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class PickupScheduleTest {

    @Test
    fun viernesPedidoRecojoLunes() {

        val friday = LocalDate.of(2026, 10, 16)

        val schedule = estimatePickupSchedule(friday)

        assertEquals(
            LocalDate.of(2026, 10, 19),
            schedule.readyAt.toLocalDate()
        )

        assertEquals(
            8,
            schedule.readyAt.hour
        )
    }

    @Test
    fun plazoTresDiasHabilesDespuesDeRecojo() {

        val friday = LocalDate.of(2026, 10, 16)

        val schedule = estimatePickupSchedule(friday)

        assertEquals(
            LocalDate.of(2026, 10, 22),
            schedule.deadlineAt.toLocalDate()
        )

        assertEquals(
            21,
            schedule.deadlineAt.hour
        )
    }
}