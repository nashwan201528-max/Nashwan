package com.example.markdownlint.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.markdownlint.model.ConfigPreset
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {

    @Query("SELECT * FROM presets")
    fun getAllPresets(): Flow<List<ConfigPreset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: ConfigPreset)

    @Update
    suspend fun updatePreset(preset: ConfigPreset)

    @Delete
    suspend fun deletePreset(preset: ConfigPreset)

    @Query("SELECT COUNT(*) FROM presets")
    suspend fun getCount(): Int
}
