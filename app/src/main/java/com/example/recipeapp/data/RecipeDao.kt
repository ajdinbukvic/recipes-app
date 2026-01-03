package com.example.recipeapp.data

import androidx.room.*
import com.example.recipeapp.model.Recipe
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes")
    fun getAll(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getById(id: Long): Recipe?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: Recipe): Long

    @Update
    suspend fun update(recipe: Recipe)

    @Delete
    suspend fun delete(recipe: Recipe)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<Recipe>)

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun getCount(): Int

    @Query("SELECT * FROM recipes WHERE naziv LIKE '%' || :q || '%'")
    fun searchByName(q: String): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE favorit = 1")
    fun getFavorites(): Flow<List<Recipe>>

    @Query("UPDATE recipes SET posjete = posjete + 1 WHERE id = :id")
    suspend fun incrementVisits(id: Long)

    @Query("UPDATE recipes SET favorit = 1 - favorit WHERE id = :id")
    suspend fun toggleFavorite(id: Long)

    @Query("SELECT * FROM recipes ORDER BY posjete DESC LIMIT :limit")
    fun topVisited(limit: Int): Flow<List<Recipe>>

    @Query("""
        SELECT * FROM recipes 
        ORDER BY createdAt DESC 
        LIMIT :limit
    """)
    fun getLastAdded(limit: Int): Flow<List<Recipe>>

    @Query("""
        SELECT * FROM recipes 
        WHERE lastViewedAt IS NOT NULL
        ORDER BY lastViewedAt DESC 
        LIMIT :limit
    """)
    fun getRecentlyViewed(limit: Int): Flow<List<Recipe>>

    @Query("""
        UPDATE recipes 
        SET posjete = posjete + 1, 
            lastViewedAt = :time 
        WHERE id = :id
    """)
    suspend fun markViewed(id: Long, time: Long = System.currentTimeMillis())

    @Query("SELECT * FROM recipes ORDER BY naziv COLLATE NOCASE ASC")
    fun getAllSortedAZ(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes ORDER BY naziv COLLATE NOCASE DESC")
    fun getAllSortedZA(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes ORDER BY posjete DESC")
    fun getMostViewed(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    fun getNewest(): Flow<List<Recipe>>

    @Query("""
        SELECT * FROM recipes 
        ORDER BY 
            CASE WHEN lastViewedAt IS NULL THEN 1 ELSE 0 END,
            lastViewedAt DESC
    """)
    fun getLastViewed(): Flow<List<Recipe>>

    @Query("""
        SELECT * FROM recipes 
        ORDER BY favorit DESC, naziv COLLATE NOCASE ASC
    """)
    fun getFavoritesFirst(): Flow<List<Recipe>>
}
