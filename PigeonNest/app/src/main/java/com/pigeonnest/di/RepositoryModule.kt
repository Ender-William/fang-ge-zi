package com.pigeonnest.di

import com.pigeonnest.data.repository.BackupRepositoryImpl
import com.pigeonnest.data.repository.FamilyRepositoryImpl
import com.pigeonnest.data.repository.LoftRepositoryImpl
import com.pigeonnest.data.repository.PigeonRepositoryImpl
import com.pigeonnest.domain.repository.BackupRepository
import com.pigeonnest.domain.repository.FamilyRepository
import com.pigeonnest.domain.repository.LoftRepository
import com.pigeonnest.domain.repository.PigeonRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPigeonRepository(
        impl: PigeonRepositoryImpl
    ): PigeonRepository

    @Binds
    @Singleton
    abstract fun bindLoftRepository(
        impl: LoftRepositoryImpl
    ): LoftRepository

    @Binds
    @Singleton
    abstract fun bindFamilyRepository(
        impl: FamilyRepositoryImpl
    ): FamilyRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(
        impl: BackupRepositoryImpl
    ): BackupRepository
}
