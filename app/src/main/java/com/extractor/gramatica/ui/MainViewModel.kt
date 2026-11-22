package com.extractor.gramatica.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extractor.gramatica.nlp.Analysis
import com.extractor.gramatica.nlp.Analyzer
import com.extractor.gramatica.nlp.Category
import com.extractor.gramatica.nlp.Lang
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ViewMode { LIST, TEXT }
enum class SortMode { FREQUENCY, ALPHABETIC }

class MainViewModel : ViewModel() {

    var input by mutableStateOf("")
        private set
    var lang by mutableStateOf(Lang.AUTO)
        private set
    var analysis by mutableStateOf<Analysis?>(null)
        private set
    var loading by mutableStateOf(false)
        private set

    var selected by mutableStateOf(Category.NOUN)
        private set
    var viewMode by mutableStateOf(ViewMode.LIST)
        private set
    var sortMode by mutableStateOf(SortMode.FREQUENCY)
        private set
    var query by mutableStateOf("")
        private set
    var highlighted by mutableStateOf(Category.values().toSet())
        private set

    private var job: Job? = null

    fun updateInput(value: String) {
        input = value
    }

    fun updateLang(value: Lang) {
        lang = value
    }

    fun selectCategory(c: Category) {
        selected = c
    }

    fun updateViewMode(m: ViewMode) {
        viewMode = m
    }

    fun updateSort(m: SortMode) {
        sortMode = m
    }

    fun updateQuery(q: String) {
        query = q
    }

    fun toggleHighlight(c: Category) {
        highlighted = if (highlighted.contains(c)) highlighted - c else highlighted + c
    }

    fun clear() {
        job?.cancel()
        input = ""
        analysis = null
        loading = false
        query = ""
    }

    fun backToInput() {
        analysis = null
        query = ""
    }

    fun analyze() {
        val text = input.trim()
        if (text.isEmpty()) return
        job?.cancel()
        loading = true
        job = viewModelScope.launch {
            val result = withContext(Dispatchers.Default) { Analyzer.analyze(text, lang) }
            analysis = result
            query = ""
            viewMode = ViewMode.LIST
            // Se abre la primera categoría que tenga resultados.
            selected = listOf(Category.NOUN, Category.VERB, Category.ADJ, Category.ADV, Category.TERM)
                .firstOrNull { result.countOf(it) > 0 } ?: Category.NOUN
            loading = false
        }
    }
}
