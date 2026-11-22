package com.extractor.gramatica.nlp

/** Categorías que muestra la app. */
enum class Category(val plural: String, val singular: String) {
    NOUN("Sustantivos", "Sustantivo"),
    ADJ("Adjetivos", "Adjetivo"),
    VERB("Verbos", "Verbo"),
    ADV("Adverbios", "Adverbio"),
    TERM("Terminología", "Término")
}

enum class Lang(val label: String) {
    AUTO("Auto"),
    ES("Español"),
    EN("English")
}

/** Clase gramatical interna que asigna el etiquetador a cada palabra. */
enum class Pos { NOUN, ADJ, VERB, ADV, FUNC, NUM, OTHER }

class Token(
    val text: String,
    val start: Int,
    val end: Int,
    val sentenceStart: Boolean
) {
    val lower: String = text.lowercase()
    var pos: Pos = Pos.OTHER
    var lemma: String = lower
    var proper: Boolean = false
    var acronym: Boolean = false

    /** false cuando el texto está en TÍTULO O MAYÚSCULAS y la mayúscula no indica nombre propio. */
    var allowProper: Boolean = true

    /** true cuando la clase salió de una regla léxica fiable (no del valor por defecto). */
    var certain: Boolean = false
}

/** Una palabra (o término) agrupada con su recuento. */
data class Entry(
    val lemma: String,
    val category: Category,
    val count: Int,
    val forms: List<String>,
    val score: Double = 0.0
)

/** Fragmento del texto original que pertenece a una categoría (para resaltar). */
data class Span(val start: Int, val end: Int, val category: Category)

data class Analysis(
    val lang: Lang,
    val text: String,
    val entries: Map<Category, List<Entry>>,
    val spans: List<Span>,
    val totalWords: Int,
    val uniqueWords: Int
) {
    fun countOf(category: Category): Int = entries[category]?.size ?: 0
}

interface Tagger {
    fun tag(tokens: List<Token>)
}
