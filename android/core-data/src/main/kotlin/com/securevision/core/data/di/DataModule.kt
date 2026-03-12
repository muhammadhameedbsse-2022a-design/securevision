package com.securevision.core.data.di

import android.content.Context
import androidx.room.Room
import com.securevision.core.data.local.AppDatabase
import com.securevision.core.data.local.dao.AlertDao
import com.securevision.core.data.local.dao.DetectionEventDao
import com.securevision.core.data.local.dao.ProfileDao
import com.securevision.core.data.repository.AlertRepositoryImpl
import com.securevision.core.data.repository.DetectionRepositoryImpl
import com.securevision.core.data.repository.ProfileRepositoryImpl
import com.securevision.core.domain.repository.AlertRepository
import com.securevision.core.domain.repository.DetectionRepository
import com.securevision.core.domain.repository.ProfileRepository
import com.securevision.core.domain.usecase.AlertCooldownManager
import com.securevision.core.domain.usecase.GetAlertsUseCase
import com.securevision.core.domain.usecase.GetDetectionHistoryUseCase
import com.securevision.core.domain.usecase.GetProfilesUseCase
import com.securevision.core.domain.usecase.MatchFaceUseCase
import com.securevision.core.domain.usecase.SaveAlertUseCase
import com.securevision.core.domain.usecase.SaveDetectionEventUseCase
import com.securevision.core.domain.usecase.SaveProfileUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(AppDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideAlertDao(database: AppDatabase): AlertDao = database.alertDao()

    @Provides
    @Singleton
    fun provideDetectionEventDao(database: AppDatabase): DetectionEventDao =
        database.detectionEventDao()

    @Provides
    @Singleton
    fun provideProfileDao(database: AppDatabase): ProfileDao = database.profileDao()
}

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideGetAlertsUseCase(alertRepository: AlertRepository): GetAlertsUseCase =
        GetAlertsUseCase(alertRepository)

    @Provides
    fun provideGetDetectionHistoryUseCase(detectionRepository: DetectionRepository): GetDetectionHistoryUseCase =
        GetDetectionHistoryUseCase(detectionRepository)

    @Provides
    fun provideSaveDetectionEventUseCase(detectionRepository: DetectionRepository): SaveDetectionEventUseCase =
        SaveDetectionEventUseCase(detectionRepository)

    @Provides
    fun provideGetProfilesUseCase(profileRepository: ProfileRepository): GetProfilesUseCase =
        GetProfilesUseCase(profileRepository)

    @Provides
    fun provideSaveAlertUseCase(alertRepository: AlertRepository): SaveAlertUseCase =
        SaveAlertUseCase(alertRepository)

    @Provides
    fun provideSaveProfileUseCase(profileRepository: ProfileRepository): SaveProfileUseCase =
        SaveProfileUseCase(profileRepository)

    @Provides
    fun provideMatchFaceUseCase(profileRepository: ProfileRepository): MatchFaceUseCase =
        MatchFaceUseCase(profileRepository)

    @Provides
    @Singleton
    fun provideAlertCooldownManager(): AlertCooldownManager =
        AlertCooldownManager()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAlertRepository(impl: AlertRepositoryImpl): AlertRepository

    @Binds
    @Singleton
    abstract fun bindDetectionRepository(impl: DetectionRepositoryImpl): DetectionRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository
}
