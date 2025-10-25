package com.lifeops.app.di

import android.content.Context
import androidx.room.Room
import com.lifeops.app.data.local.LifeOpsDatabase
import com.lifeops.app.data.local.dao.SupplyDao
import com.lifeops.app.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependencies
 * Provides Room database and DAOs as singletons
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideLifeOpsDatabase(
        @ApplicationContext context: Context
    ): LifeOpsDatabase {
        return Room.databaseBuilder(
            context,
            LifeOpsDatabase::class.java,
            LifeOpsDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // TODO: Replace with proper migrations in production
            .build()
    }
    
    @Provides
    @Singleton
    fun provideTaskDao(database: LifeOpsDatabase): TaskDao {
        return database.taskDao()
    }
    
    @Provides
    @Singleton
    fun provideSupplyDao(database: LifeOpsDatabase): SupplyDao {
        return database.supplyDao()
    }
}
