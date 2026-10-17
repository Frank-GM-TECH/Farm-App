package com.example.farmapp.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Room valida estas consultas SQL en compilación mediante KSP. */
@Dao
interface RoomDemoDao {
    @Query("SELECT * FROM articulos_room_demo ORDER BY nombre COLLATE NOCASE ASC, id ASC")
    fun observeAll(): Flow<List<RoomDemoItem>>

    @Query("SELECT * FROM articulos_room_demo WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): RoomDemoItem?

    /** Flow permite que detalles, edición e inventario observen la misma fila. */
    @Query("SELECT * FROM articulos_room_demo WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<RoomDemoItem?>

    /** Venta ATÓMICA: se evita leer stock y restar en Kotlin (condición de carrera). */
    @Query("UPDATE articulos_room_demo SET stock = stock - 1 WHERE id = :id AND stock > 0")
    suspend fun sellOneIfAvailable(id: Long): Int

    @Query("DELETE FROM articulos_room_demo WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Insert
    suspend fun insert(item: RoomDemoItem): Long

    @Update
    suspend fun update(item: RoomDemoItem): Int

    @Delete
    suspend fun delete(item: RoomDemoItem): Int
}
