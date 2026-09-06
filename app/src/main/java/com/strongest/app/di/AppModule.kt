package com.strongest.app.di

import android.content.Context
import androidx.room.Room
import com.strongest.app.data.db.ExerciseDao
import com.strongest.app.data.db.MeasurementEntryDao
import com.strongest.app.data.db.RoutineDao
import com.strongest.app.data.db.StrongestDatabase
import com.strongest.app.data.db.WorkoutDao
import com.strongest.app.data.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StrongestDatabase {
        return Room.databaseBuilder(
            context,
            StrongestDatabase::class.java,
            StrongestDatabase.DATABASE_NAME
        )
            .addMigrations(
                StrongestDatabase.MIGRATION_1_2,
                StrongestDatabase.MIGRATION_2_3,
                StrongestDatabase.MIGRATION_3_4,
                StrongestDatabase.MIGRATION_4_5
            )
            .build()
    }

    @Provides
    fun provideExerciseDao(database: StrongestDatabase): ExerciseDao = database.exerciseDao()

    @Provides
    fun provideRoutineDao(database: StrongestDatabase): RoutineDao = database.routineDao()

    @Provides
    fun provideWorkoutDao(database: StrongestDatabase): WorkoutDao = database.workoutDao()

    @Provides
    fun provideMeasurementEntryDao(database: StrongestDatabase): MeasurementEntryDao = database.measurementEntryDao()

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepository(context)
    }
}
