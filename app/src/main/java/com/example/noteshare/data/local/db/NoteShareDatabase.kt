package com.example.noteshare.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.noteshare.data.local.db.dao.*
import com.example.noteshare.data.model.*

@Database(
    entities = [
        Note::class,
        MoodEntry::class,
        Event::class,
        User::class,
        Pair::class,
        OutboxEventEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class NoteShareDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun moodDao(): MoodDao
    abstract fun eventDao(): EventDao
    abstract fun userDao(): UserDao
    abstract fun pairDao(): PairDao
    abstract fun outboxDao(): OutboxDao
}
