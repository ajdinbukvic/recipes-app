package com.example.recipeapp.repository

import com.example.recipeapp.data.RecipeDao
import com.example.recipeapp.model.Recipe
import com.example.recipeapp.ui.utils.SortType
import com.example.recipeapp.ui.utils.localeComparatorAsc
import com.example.recipeapp.ui.utils.localeComparatorDesc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecipeRepository(private val dao: RecipeDao) {
    fun getAll(): Flow<List<Recipe>> = dao.getAll()
    suspend fun getById(id: Long) = dao.getById(id)
    suspend fun insert(recipe: Recipe) = dao.insert(recipe)
    suspend fun update(recipe: Recipe) = dao.update(recipe)
    suspend fun delete(recipe: Recipe) = dao.delete(recipe)
    fun search(q: String): Flow<List<Recipe>> = dao.searchByName(q)
    fun favorites(): Flow<List<Recipe>> = dao.getFavorites()
    suspend fun incrementVisits(id: Long) = dao.incrementVisits(id)
    fun topVisited(limit: Int): Flow<List<Recipe>> = dao.topVisited(limit)

    suspend fun toggleFavorite(id: Long) {
        dao.toggleFavorite(id)
    }

    fun getLastAdded(limit: Int): Flow<List<Recipe>> = dao.getLastAdded(limit)
    fun getRecentlyViewed(limit: Int): Flow<List<Recipe>> = dao.getRecentlyViewed(limit)
    suspend fun markViewed(id: Long, time: Long = System.currentTimeMillis()) = dao.markViewed(id, time);

    /*fun getAllSorted(sortType: SortType): Flow<List<Recipe>> {
        return when (sortType) {
            SortType.DEFAULT -> dao.getAll()
            SortType.AZ -> dao.getAllSortedAZ()
            SortType.ZA -> dao.getAllSortedZA()
            SortType.MOST_VIEWED -> dao.getMostViewed()
            SortType.NEWEST -> dao.getNewest()
            SortType.LAST_VIEWED -> dao.getLastViewed()
            SortType.FAVORITES_FIRST -> dao.getFavoritesFirst()
        }
    }*/

    fun getAllSorted(sortType: SortType): Flow<List<Recipe>> {
        return dao.getAll().map { list ->
            when (sortType) {
                SortType.DEFAULT -> list
                SortType.AZ -> list.sortedWith(localeComparatorAsc)
                SortType.ZA -> list.sortedWith(localeComparatorDesc)
                SortType.MOST_VIEWED -> list.sortedByDescending { it.posjete }
                SortType.NEWEST -> list.sortedByDescending { it.createdAt }
                SortType.LAST_VIEWED -> list.sortedByDescending { it.lastViewedAt ?: 0L }
                SortType.FAVORITES_FIRST -> list.sortedByDescending { it.favorit }
            }
        }
    }
}
