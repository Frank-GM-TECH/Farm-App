package com.example.farmapp.data.room

/** Validación Kotlin puro, independiente de Android y de Room. */
data class RoomDemoValidInput(val name: String, val priceCents: Long, val stock: Int)

object RoomDemoForm {
    private val pricePattern = Regex("^[0-9]{1,6}([.,][0-9]{1,2})?$")

    fun validate(nameText: String, priceText: String, stockText: String): RoomDemoValidInput? {
        val name = nameText.trim()
        val price = priceText.trim()
        val stock = stockText.trim().toIntOrNull() ?: return null
        if (name.isEmpty() || name.length > 70 || stock !in 0..99_999 || !pricePattern.matches(price)) {
            return null
        }
        val pieces = price.replace(',', '.').split('.')
        val cents = pieces[0].toLong() * 100 + pieces.getOrNull(1).orEmpty().padEnd(2, '0').toLongOrNull().orZero()
        return RoomDemoValidInput(name, cents, stock)
    }

    private fun Long?.orZero(): Long = this ?: 0L
}
