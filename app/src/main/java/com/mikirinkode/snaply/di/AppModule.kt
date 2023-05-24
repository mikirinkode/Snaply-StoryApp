package com.mikirinkode.snaply.di

import android.content.Context
import androidx.room.Room
import com.mikirinkode.snaply.data.repository.StoryRepository
import com.mikirinkode.snaply.data.repository.UserRepository
import com.mikirinkode.snaply.data.source.local.StoryDao
import com.mikirinkode.snaply.data.source.local.SnaplyDatabase
import com.mikirinkode.snaply.data.source.remote.ApiService
import com.mikirinkode.snaply.utils.AppExecutors
import com.mikirinkode.snaply.utils.Constants
import com.mikirinkode.snaply.utils.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient{
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePreferences(@ApplicationContext context: Context): Preferences {
        return Preferences(context)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext app: Context): SnaplyDatabase {
        val passphrase: ByteArray = SQLiteDatabase.getBytes(Constants.PASSPHRASE.toCharArray())
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(app, SnaplyDatabase::class.java, Constants.DB_NAME)
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    @Singleton
    fun provideDao(db: SnaplyDatabase): StoryDao {
        return db.storyDao()
    }

    @Provides
    @Singleton
    fun provideAppExecutors(): AppExecutors {
        return AppExecutors()
    }

    @Provides
    @Singleton
    fun provideStoryRepository(apiService: ApiService, database: SnaplyDatabase, dao: StoryDao, pref: Preferences, appExecutors: AppExecutors): StoryRepository {
        return StoryRepository(apiService, database, dao, pref, appExecutors)
    }

    @Provides
    @Singleton
    fun provideUserRepository(apiService: ApiService, dao: StoryDao, preferences: Preferences, appExecutors: AppExecutors): UserRepository {
        return UserRepository(apiService, dao, appExecutors, preferences)
    }
}