package com.kelompok10.eeducation.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader

@Database(entities = [Materi::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun materiDao(): MateriDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "education_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Populate initial data when database is created
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                populateDatabase(context, database.materiDao())
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateDatabase(context: Context, materiDao: MateriDao) {
            try {
                // Load data from JSON file in assets
                val inputStream = context.assets.open("materi_awal.json")
                val reader = InputStreamReader(inputStream)
                
                val gson = Gson()
                val listType = object : TypeToken<List<Materi>>() {}.type
                val materiList: List<Materi> = gson.fromJson(reader, listType)
                
                reader.close()
                inputStream.close()
                
                // Insert data into database
                materiDao.insertAll(materiList)
            } catch (e: Exception) {
                e.printStackTrace()
                // If JSON loading fails, you can fallback to hardcoded data or leave empty
            }
        }
    }
}