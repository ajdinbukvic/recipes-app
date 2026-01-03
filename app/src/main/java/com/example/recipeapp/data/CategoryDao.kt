package com.example.recipeapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.recipeapp.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories")
    fun getAll(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<Category>)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int

    @Query("""
    SELECT c.id, c.name, COUNT(r.id) AS recipeCount
    FROM categories c
    LEFT JOIN recipes r ON r.category_id = c.id
    GROUP BY c.id
    ORDER BY c.name
""")
    fun getCategoriesWithCount(): Flow<List<CategoryWithCount>>
}