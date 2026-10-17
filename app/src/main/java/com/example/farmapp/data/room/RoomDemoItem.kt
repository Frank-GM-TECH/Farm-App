package com.example.farmapp.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Tabla educativa NUEVA; no migra datos SQL C24 ni representa inventario real. */
@Entity(tableName = "articulos_room_demo")
data class RoomDemoItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "nombre") val name: String,
    @ColumnInfo(name = "precio_centimos") val priceCents: Long,
    @ColumnInfo(name = "stock") val stock: Int
)
