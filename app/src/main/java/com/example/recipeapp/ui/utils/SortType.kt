package com.example.recipeapp.ui.utils

enum class SortType(val value: String) {
    DEFAULT("Početno (osnovno)"),
    AZ("Po abecedi (A-Z)"),
    ZA("Po abecedi (Z-A)"),
    MOST_VIEWED("Najposjećeniji"),
    NEWEST("Zadnje dodani"),
    LAST_VIEWED("Nedavno pregledani"),
    FAVORITES_FIRST("Favoriti")
}