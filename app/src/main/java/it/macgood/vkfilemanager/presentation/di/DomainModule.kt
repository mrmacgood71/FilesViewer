package it.macgood.vkfilemanager.presentation.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.macgood.data.repository.FileRepositoryImpl
import it.macgood.domain.repository.FileRepository

@Module
@InstallIn(SingletonComponent::class)
interface DomainModule {

    @Binds
    fun provideFileRepository(
        repository: FileRepositoryImpl
    ) : FileRepository
}