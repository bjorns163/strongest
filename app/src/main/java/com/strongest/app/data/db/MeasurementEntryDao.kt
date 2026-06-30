package com.strongest.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.strongest.app.data.model.BodyMetric
import com.strongest.app.data.model.MeasurementEntry
import kotlinx.coroutines.flow.Flow

data class LatestMeasurement(
    val metric: String,
    val value: Float,
    val timestamp: Long,
    val entryCount: Int
)

@Dao
interface MeasurementEntryDao {
    @Query("SELECT * FROM measurement_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<MeasurementEntry>>

    @Query("SELECT * FROM measurement_entries ORDER BY timestamp DESC")
    suspend fun getAllEntriesList(): List<MeasurementEntry>

    @Query("SELECT * FROM measurement_entries WHERE metric = :metric ORDER BY timestamp ASC")
    fun getEntriesForMetric(metric: BodyMetric): Flow<List<MeasurementEntry>>

    @Query("""
        SELECT e.metric AS metric, e.value AS value, e.timestamp AS timestamp, c.cnt AS entryCount
        FROM measurement_entries e
        JOIN (
            SELECT metric, COUNT(*) AS cnt, MAX(timestamp) AS maxTs
            FROM measurement_entries
            GROUP BY metric
        ) c ON c.metric = e.metric AND c.maxTs = e.timestamp
    """)
    fun getLatestPerMetric(): Flow<List<LatestMeasurement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: MeasurementEntry): Long

    @Update
    suspend fun updateEntry(entry: MeasurementEntry)

    @Delete
    suspend fun deleteEntry(entry: MeasurementEntry)
}
