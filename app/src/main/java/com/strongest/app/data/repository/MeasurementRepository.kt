package com.strongest.app.data.repository

import com.strongest.app.data.db.LatestMeasurement
import com.strongest.app.data.db.MeasurementEntryDao
import com.strongest.app.data.model.BodyMetric
import com.strongest.app.data.model.MeasurementEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeasurementRepository @Inject constructor(
    private val dao: MeasurementEntryDao
) {
    fun getAllEntries(): Flow<List<MeasurementEntry>> = dao.getAllEntries()

    fun getEntriesForMetric(metric: BodyMetric): Flow<List<MeasurementEntry>> =
        dao.getEntriesForMetric(metric)

    fun getLatestPerMetric(): Flow<List<LatestMeasurement>> = dao.getLatestPerMetric()

    suspend fun addEntry(metric: BodyMetric, value: Float, timestamp: Long, notes: String = ""): Long =
        dao.insertEntry(MeasurementEntry(metric = metric, value = value, timestamp = timestamp, notes = notes))

    suspend fun deleteEntry(entry: MeasurementEntry) = dao.deleteEntry(entry)

    suspend fun updateEntry(entry: MeasurementEntry) = dao.updateEntry(entry)
}
