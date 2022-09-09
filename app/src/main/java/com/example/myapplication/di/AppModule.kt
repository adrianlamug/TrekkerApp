package com.example.myapplication.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.example.myapplication.db.TrekkDatabase
import com.example.myapplication.other.Constants.KEY_FIRST_TOGGLE
import com.example.myapplication.other.Constants.KEY_NAME
import com.example.myapplication.other.Constants.RUNNING_DATABASE_NAME
import com.example.myapplication.other.Constants.SHARED_PREFERENCES
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    // builds the user database
    @Singleton
    @Provides
    fun provideRunningDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        TrekkDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideRunDao(db: TrekkDatabase) = db.getTrekkDao()


    // allows other fragments to get
    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context) =
            app.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPreferences: SharedPreferences) = sharedPreferences.getString(KEY_NAME, "") ?: ""

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPreferences: SharedPreferences) = sharedPreferences.getBoolean(KEY_FIRST_TOGGLE, true)
}