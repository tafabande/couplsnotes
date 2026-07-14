package com.example.noteshare.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.noteshare.data.local.db.NoteShareDatabase
import com.example.noteshare.data.local.db.dao.EventDao
import com.example.noteshare.data.local.db.dao.MoodDao
import com.example.noteshare.data.local.db.dao.NoteDao
import com.example.noteshare.data.local.db.dao.OutboxDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "noteshare_prefs")

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NoteShareDatabase {
        return Room.databaseBuilder(
            context,
            NoteShareDatabase::class.java,
            "noteshare_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideNoteDao(db: NoteShareDatabase): NoteDao = db.noteDao()

    @Provides
    fun provideMoodDao(db: NoteShareDatabase): MoodDao = db.moodDao()

    @Provides
    fun provideEventDao(db: NoteShareDatabase): EventDao = db.eventDao()

    @Provides
    fun provideOutboxDao(db: NoteShareDatabase): OutboxDao = db.outboxDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}
