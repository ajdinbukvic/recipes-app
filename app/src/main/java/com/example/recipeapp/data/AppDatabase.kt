package com.example.recipeapp.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.recipeapp.model.Category
import com.example.recipeapp.model.Recipe
import com.example.recipeapp.model.RecipeImage

@Database(
    entities = [Recipe::class, Category::class, RecipeImage::class],
    version = 10
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun categoryDao(): CategoryDao

    abstract fun recipeImageDao(): RecipeImageDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recipe_db"
                )
                    .fallbackToDestructiveMigration(false) // ⬅⬅ KLJUČNO
                    .build()
                INSTANCE = instance
                instance
            }
        }
        /*val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE recipes ADD COLUMN napomena TEXT DEFAULT ''"
                )
            }
        }*/
    }
}
