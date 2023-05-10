package it.macgood.vkfilemanager.presentation.di

import android.content.Context
import android.os.Environment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import it.macgood.data.database.FileDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context) = FileDatabase.invoke(context)

    @Provides
    @Singleton
    fun provideFileDao(database: FileDatabase) = database.getFileDao()

}