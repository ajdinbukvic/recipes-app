package com.example.recipeapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipes",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("category_id")]
)
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val naziv: String,
    @ColumnInfo(defaultValue = "''") val sastojci: String = "",
    @ColumnInfo(defaultValue = "''") val postupak: String = "",
    @ColumnInfo(defaultValue = "''") val napomena: String = "", // <- ovo
    val posjete: Int = 0,
    val favorit: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastViewedAt: Long? = null,
    @ColumnInfo(name = "category_id")
    val categoryId: Long? = null
)
