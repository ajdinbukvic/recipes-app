package com.example.recipeapp.ui.utils

import com.example.recipeapp.model.Recipe
import java.text.Collator
import java.util.Locale

private val bosnianLocale = Locale("bs", "BA")

private val collator: Collator = Collator.getInstance(bosnianLocale).apply {
    strength = Collator.PRIMARY
}

val localeComparatorAsc = Comparator<Recipe> { a, b ->
    collator.compare(a.naziv, b.naziv)
}

val localeComparatorDesc = Comparator<Recipe> { a, b ->
    collator.compare(b.naziv, a.naziv)
}