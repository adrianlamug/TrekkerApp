package com.example.myapplication.db
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Trekk::class],
    version = 1
)

@TypeConverters(Converters::class)
abstract class TrekkDatabase : RoomDatabase() {
    abstract fun getTrekkDao(): TrekkDAO
}