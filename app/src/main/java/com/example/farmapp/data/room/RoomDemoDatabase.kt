package com.example.farmapp.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

/** Solo la tabla Room del laboratorio; versión y nombre distintos de SQLite del C24. */
@Database(entities = [RoomDemoItem::class], version = 1, exportSchema = false)
abstract class RoomDemoDatabase : RoomDatabase() {
    abstract fun roomDemoDao(): RoomDemoDao
}
