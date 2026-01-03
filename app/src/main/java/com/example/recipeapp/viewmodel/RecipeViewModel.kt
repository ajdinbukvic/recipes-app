package com.example.recipeapp.viewmodel

import android.app.Application
import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeapp.data.AppDatabase
import com.example.recipeapp.model.Recipe
import com.example.recipeapp.repository.CategoryRepository
import com.example.recipeapp.repository.RecipeRepository
import com.example.recipeapp.ui.utils.SortType
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.Collator
import java.util.Locale

class RecipeViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: RecipeRepository
    private val catRepo: CategoryRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repo = RecipeRepository(db.recipeDao())
        catRepo = CategoryRepository(db.categoryDao())
        loadAll()
    }

    val categories = catRepo.categories
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    init {
        viewModelScope.launch {
            catRepo.seedIfEmpty()
        }
    }

    private val _sortType = MutableStateFlow(SortType.DEFAULT)
    val sortType = _sortType.asStateFlow()

    private val _searchRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    private val _allRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    private val _favRecipes = MutableStateFlow<List<Recipe>>(emptyList())

    private val _topRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    private val _lastAdded = MutableStateFlow<List<Recipe>>(emptyList())
    private val _recentlyViewed = MutableStateFlow<List<Recipe>>(emptyList())

    val searchRecipes: StateFlow<List<Recipe>> = _searchRecipes
    //val allRecipes: StateFlow<List<Recipe>> = _allRecipes
    val favRecipes: StateFlow<List<Recipe>> = _favRecipes

    val topRecipes: StateFlow<List<Recipe>> = _topRecipes
    val lastAdded: StateFlow<List<Recipe>> = _lastAdded
    val recentlyViewed: StateFlow<List<Recipe>> = _recentlyViewed

    val categoriesWithCount = catRepo.categoriesWithCount
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val allRecipes = _sortType
        .flatMapLatest { sort ->
            repo.getAllSorted(sort)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun setSort(sort: SortType) {
        _sortType.value = sort
    }


    fun loadAll() = viewModelScope.launch {
        repo.getAll().collect { _allRecipes.value = it }
    }

    fun search(q: String) = viewModelScope.launch {
        repo.search(q).collect { _searchRecipes.value = it }
    }

    fun loadFavorites() = viewModelScope.launch {
        repo.favorites().collect { _favRecipes.value = it }
    }

    fun loadTop(limit: Int = 10) = viewModelScope.launch {
        repo.topVisited(limit).collect { _topRecipes.value = it }
    }

    fun loadLastAdded(limit: Int = 10) = viewModelScope.launch {
        repo.getLastAdded(limit).collect { _lastAdded.value = it }
    }

    fun loadRecentlyViewed(limit: Int = 10) = viewModelScope.launch {
        repo.getRecentlyViewed(limit).collect { _recentlyViewed.value = it }
    }

    fun insert(recipe: Recipe, onDone: (Long) -> Unit = {}) = viewModelScope.launch {
        val id = repo.insert(recipe)
        onDone(id)
        loadAll()
    }

    fun update(recipe: Recipe) = viewModelScope.launch {
        repo.update(recipe)
        loadAll()
    }

    fun delete(recipe: Recipe) = viewModelScope.launch {
        repo.delete(recipe)
        loadAll()
    }

    fun incrementVisits(id: Long) = viewModelScope.launch {
        repo.incrementVisits(id)
    }

    fun incrementVisitsAndRecentlyViewed(id: Long, time: Long = System.currentTimeMillis()) = viewModelScope.launch {
        repo.markViewed(id, time)
    }

    fun toggleFavorite(id: Long) = viewModelScope.launch {
        repo.toggleFavorite(id)
        loadAll() // ili loadFavorites() ako si na favorites stranici
    }

    suspend fun getByIdSuspend(id: Long): Recipe? {
        return repo.getById(id)
    }

    fun clearSearchResults() {
        _searchRecipes.value = emptyList()
    }

    /*fun filterByCategory(categoryId: Long) {
        val currentList = _searchRecipes.value
        val filtered = currentList.filter { it.categoryId == categoryId }
        _searchRecipes.value = filtered
    }*/

    fun filterByCategory(categoryId: Long) {
        // filtriraj iz _allRecipes, NE iz već filtrirane liste
        val filtered = _allRecipes.value.filter { it.categoryId == categoryId }
        _searchRecipes.value = filtered
    }

    fun exportDatabaseToDownloads(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Dohvati sve recepte
                val recipes = repo.getAll().first()
                val json = GsonBuilder().setPrettyPrinting().create().toJson(recipes)

                // Kreiraj fajl u Downloads folderu
                val downloadsFolder = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                if (downloadsFolder?.exists() == false) downloadsFolder.mkdirs()

                val file = File(downloadsFolder, "recipes_backup_${System.currentTimeMillis()}.json")
                file.writeText(json)

                // Opcionalno: Toast na glavnom thread-u
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Baza exportovana: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Greška pri exportu: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
