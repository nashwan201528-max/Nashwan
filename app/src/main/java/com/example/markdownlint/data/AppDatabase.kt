package com.example.markdownlint.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.markdownlint.engine.PresetCatalog
import com.example.markdownlint.model.ConfigPreset
import com.example.markdownlint.model.MarkdownDocument
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [MarkdownDocument::class, ConfigPreset::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun documentDao(): DocumentDao
    abstract fun presetDao(): PresetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "markdownlint_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        // Populate default sample documents
                        SampleDocuments.allSamples.forEach { sampleDoc ->
                            database.documentDao().insertDocument(sampleDoc)
                        }
                        // Populate built-in presets
                        PresetCatalog.builtInPresets.forEach { preset ->
                            database.presetDao().insertPreset(preset)
                        }
                    }
                }
            }
        }
    }
}
