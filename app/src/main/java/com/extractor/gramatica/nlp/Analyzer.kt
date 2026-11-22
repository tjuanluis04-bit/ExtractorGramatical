package com.extractor.gramatica.nlp

/** Punto de entrada del análisis: tokeniza, detecta idioma, etiqueta y agrupa los resultados. */
object Analyzer {

    private val wordRegex = Regex("[\\p{L}\\p{N}][\\p{L}\\p{N}\\p{M}'’\\-]*")

    private class Agg(var display: String, val category: Category) {
        var count: Int = 0
        val forms: LinkedHashMap<String, Int> = LinkedHashMap()
    }

    fun analyze(text: String, requested: Lang): Analysis {
        val tokens = tokenize(text)
        val lang = if (requested == Lang.AUTO) detect(tokens) else requested
        val tagger: Tagger = if (lang == Lang.EN) EnglishTagger else SpanishTagger
        tagger.tag(tokens)

        val groups = HashMap<Category, LinkedHashMap<String, Agg>>()
        for (c in listOf(Category.NOUN, Category.ADJ, Category.VERB, Category.ADV)) {
            groups[c] = LinkedHashMap()
        }
        val spans = ArrayList<Span>()
        val distinct = HashSet<String>()

        for (t in tokens) {
            distinct.add(t.lower)
            val cat = when (t.pos) {
                Pos.NOUN -> Category.NOUN
                Pos.ADJ -> Category.ADJ
                Pos.VERB -> Category.VERB
                Pos.ADV -> Category.ADV
                else -> null
            } ?: continue
            if (t.lower.length < 2) continue
            spans.add(Span(t.start, t.end, cat))

            val map = groups.getValue(cat)
            val key = if (cat == Category.VERB) t.lemma else t.lower
            val display = if (t.proper && !t.acronym) t.text else if (t.acronym) t.text else key
            val agg = map.getOrPut(key) { Agg(display, cat) }
            agg.count += 1
            agg.forms[t.lower] = (agg.forms[t.lower] ?: 0) + 1
        }

        val entries = LinkedHashMap<Category, List<Entry>>()
        for (cat in listOf(Category.NOUN, Category.ADJ, Category.VERB, Category.ADV)) {
            val map = groups.getValue(cat)
            if (cat == Category.NOUN || cat == Category.ADJ || (cat == Category.VERB && lang == Lang.EN)) {
                mergeInflections(map, lang)
            }
            entries[cat] = map.values
                .map { a ->
                    val formList = a.forms.keys.filter { it != a.display.lowercase() }
                    Entry(a.display, cat, a.count, formList)
                }
                .sortedWith(compareByDescending<Entry> { it.count }.thenBy { it.lemma.lowercase() })
        }

        val (terms, termSpans) = Terminology.extract(text, tokens)
        entries[Category.TERM] = terms
        spans.addAll(termSpans)
        spans.sortBy { it.start }

        return Analysis(lang, text, entries, spans, tokens.size, distinct.size)
    }

    // ------------------------------------------------------------------ tokenización

    private fun tokenize(text: String): List<Token> {
        val matches = wordRegex.findAll(text).toList()
        var upper = 0
        var letters = 0
        for (c in text) {
            if (c.isLetter()) {
                letters++
                if (c.isUpperCase()) upper++
            }
        }
        val allCaps = letters > 10 && upper * 10 >= letters * 7

        // ¿Cuántas palabras con mayúscula inicial hay a mitad de frase? Si son demasiadas, es un texto en Título.
        val out = ArrayList<Token>(matches.size)
        var sentenceStart = true
        var prevEnd = 0
        var capMid = 0
        var midCount = 0
        for (m in matches) {
            val gap = text.substring(prevEnd, m.range.first)
            if (gap.any { it == '.' || it == '!' || it == '?' || it == '\n' || it == ':' || it == '¡' || it == '¿' ||
                    it == '•' || it == '·' || it == ';' }
            ) {
                sentenceStart = true
            }
            val raw = m.value.trim('-', '\'', '’')
            if (raw.isEmpty()) {
                prevEnd = m.range.last + 1
                continue
            }
            val start = m.range.first + (m.value.length - m.value.trimStart('-', '\'', '’').length)
            val end = start + raw.length
            val tok = Token(raw, start, end, sentenceStart)
            if (!sentenceStart) {
                midCount++
                if (raw[0].isUpperCase()) capMid++
            }
            out.add(tok)
            sentenceStart = false
            prevEnd = m.range.last + 1
        }
        val titleCase = midCount >= 6 && capMid * 10 >= midCount * 5
        for (t in out) {
            if (allCaps || titleCase) {
                t.allowProper = false
            } else if (t.text.length >= 2 && t.text.all { it.isUpperCase() || it.isDigit() } &&
                t.text.any { it.isLetter() }
            ) {
                t.acronym = true
            }
        }
        return out
    }

    private fun detect(tokens: List<Token>): Lang {
        var es = 0
        var en = 0
        for (t in tokens) {
            val w = t.lower
            if (SpanishLexicon.stopwords.contains(w)) es++
            if (EnglishTagger.stopwords.contains(w)) en++
            if (w.any { it in "áéíóúñ¿¡" }) es += 2
        }
        return if (en > es) Lang.EN else Lang.ES
    }

    // ------------------------------------------------------------------ plurales / género

    private fun baseCandidates(w: String, lang: Lang): List<String> {
        val out = ArrayList<String>()
        val sing = ArrayList<String>()
        if (lang == Lang.EN) {
            if (w.endsWith("ies") && w.length > 4) sing.add(w.dropLast(3) + "y")
            if (w.endsWith("es") && w.length > 4) sing.add(w.dropLast(2))
            if (w.endsWith("s") && w.length > 3) sing.add(w.dropLast(1))
            if (w.endsWith("ing") && w.length > 5) {
                sing.add(w.dropLast(3))
                sing.add(w.dropLast(3) + "e")
            }
            if (w.endsWith("ed") && w.length > 4) {
                sing.add(w.dropLast(2))
                sing.add(w.dropLast(1))
            }
            out.addAll(sing)
            return out.distinct().filter { it != w }
        }
        if (w.endsWith("ces") && w.length > 4) sing.add(w.dropLast(3) + "z")
        if (w.endsWith("es") && w.length > 4) sing.add(w.dropLast(2))
        if (w.endsWith("s") && w.length > 3) sing.add(w.dropLast(1))
        out.addAll(sing)
        val bases = ArrayList<String>()
        bases.add(w)
        bases.addAll(sing)
        for (b in bases) {
            if (b.endsWith("a") && b.length > 3) out.add(b.dropLast(1) + "o")
            if (b.endsWith("ora") && b.length > 5) out.add(b.dropLast(1))
        }
        return out.distinct().filter { it != w }
    }

    /** Une plurales y femeninos con su forma base solo cuando esa base aparece también en el texto. */
    private fun mergeInflections(map: LinkedHashMap<String, Agg>, lang: Lang) {
        val keys = map.keys.toList().sortedByDescending { it.length }
        for (k in keys) {
            val agg = map[k] ?: continue
            for (c in baseCandidates(k, lang)) {
                val target = map[c]
                if (target != null && target !== agg) {
                    target.count += agg.count
                    for ((f, n) in agg.forms) target.forms[f] = (target.forms[f] ?: 0) + n
                    map.remove(k)
                    break
                }
            }
        }
    }
}
