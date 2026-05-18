package com.pigeonnest.di

import com.pigeonnest.domain.repository.FamilyRepository
import com.pigeonnest.domain.repository.LoftRepository
import com.pigeonnest.domain.repository.PigeonRepository
import com.pigeonnest.domain.usecase.family.GetFamilyGraphUseCase
import com.pigeonnest.domain.usecase.family.GetLineageUseCase
import com.pigeonnest.domain.usecase.family.UpdateFamilyRelationUseCase
import com.pigeonnest.domain.usecase.loft.DeleteLoftUseCase
import com.pigeonnest.domain.usecase.loft.GetLoftListUseCase
import com.pigeonnest.domain.usecase.loft.SaveLoftUseCase
import com.pigeonnest.domain.usecase.pigeon.DeletePigeonUseCase
import com.pigeonnest.domain.usecase.pigeon.GetPigeonDetailUseCase
import com.pigeonnest.domain.usecase.pigeon.GetPigeonListUseCase
import com.pigeonnest.domain.usecase.pigeon.SavePigeonUseCase
import com.pigeonnest.data.file.PhotoStorageManager
import com.pigeonnest.domain.usecase.pigeon.SearchPigeonsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetPigeonListUseCase(pigeonRepository: PigeonRepository): GetPigeonListUseCase {
        return GetPigeonListUseCase(pigeonRepository)
    }

    @Provides
    @Singleton
    fun provideSearchPigeonsUseCase(pigeonRepository: PigeonRepository): SearchPigeonsUseCase {
        return SearchPigeonsUseCase(pigeonRepository)
    }

    @Provides
    @Singleton
    fun provideGetPigeonDetailUseCase(
        pigeonRepository: PigeonRepository,
        familyRepository: FamilyRepository
    ): GetPigeonDetailUseCase {
        return GetPigeonDetailUseCase(pigeonRepository, familyRepository)
    }

    @Provides
    @Singleton
    fun provideSavePigeonUseCase(
        pigeonRepository: PigeonRepository,
        loftRepository: LoftRepository,
        updateFamilyRelationUseCase: UpdateFamilyRelationUseCase,
        photoStorage: PhotoStorageManager
    ): SavePigeonUseCase {
        return SavePigeonUseCase(pigeonRepository, loftRepository, updateFamilyRelationUseCase, photoStorage)
    }

    @Provides
    @Singleton
    fun provideDeletePigeonUseCase(pigeonRepository: PigeonRepository): DeletePigeonUseCase {
        return DeletePigeonUseCase(pigeonRepository)
    }

    @Provides
    @Singleton
    fun provideGetLoftListUseCase(loftRepository: LoftRepository): GetLoftListUseCase {
        return GetLoftListUseCase(loftRepository)
    }

    @Provides
    @Singleton
    fun provideSaveLoftUseCase(loftRepository: LoftRepository): SaveLoftUseCase {
        return SaveLoftUseCase(loftRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteLoftUseCase(loftRepository: LoftRepository): DeleteLoftUseCase {
        return DeleteLoftUseCase(loftRepository)
    }

    @Provides
    @Singleton
    fun provideGetFamilyGraphUseCase(familyRepository: FamilyRepository): GetFamilyGraphUseCase {
        return GetFamilyGraphUseCase(familyRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateFamilyRelationUseCase(familyRepository: FamilyRepository): UpdateFamilyRelationUseCase {
        return UpdateFamilyRelationUseCase(familyRepository)
    }

    @Provides
    @Singleton
    fun provideGetLineageUseCase(familyRepository: FamilyRepository): GetLineageUseCase {
        return GetLineageUseCase(familyRepository)
    }
}
