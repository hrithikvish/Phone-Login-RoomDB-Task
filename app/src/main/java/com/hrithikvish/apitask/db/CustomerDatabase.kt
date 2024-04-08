package com.hrithikvish.apitask.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hrithikvish.apitask.model.Data

@Database(entities = [Data::class], version = 1)
@TypeConverters(CustomerDataTypeConverters::class)
abstract class CustomerDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao

    companion object {
        private const val Database_NAME = "customerDb"

        @Volatile
        private var INSTANCE: CustomerDatabase? = null
        fun getInstance(context: Context): CustomerDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        CustomerDatabase::class.java,
                        Database_NAME
                    ).allowMainThreadQueries().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}