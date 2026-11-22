package com.extractor.gramatica.nlp

/** Extrae terminología: sintagmas nominales técnicos, siglas, nombres propios y palabras especializadas. */
object Terminology {

    private val technicalSuffixes = listOf(
        "logía", "ología", "grafía", "metría", "scopía", "patía", "terapia", "cracia", "ismo", "itis",
        "osis", "ónico", "ónica", "ático", "ática", "ítico", "ítica", "ización", "izar", "ology",
        "graphy", "metry", "ization", "ometer"
    )

    private val linkWords = hashSetOf("de", "del", "of")
    private val linkDeterminers = hashSetOf("la", "el", "los", "las", "un", "una", "the")

    private class Candidate(
        val key: String,
        val display: String,
        val start: Int,
        val end: Int,
        val words: Int,
        val technical: Int,
        val proper: Boolean
    )

    private fun technicalness(t: Token): Int {
        var score = 0
        val w = t.lower
        if (t.acronym) score += 2
        if (w.length >= 11) score += 1
        for (s in technicalSuffixes) {
            if (w.length >= s.length + 4 && w.endsWith(s)) {
                score += 1
                break
            }
        }
        if (w.length >= 10 &&
            (w.endsWith("ción") || w.endsWith("miento") || w.endsWith("idad") || w.endsWith("tion") ||
                w.endsWith("ment") || w.endsWith("ity"))
        ) {
            score += 1
        }
        if (w.length >= 8 && (w.endsWith("ico") || w.endsWith("ica") || w.endsWith("icos") ||
                w.endsWith("icas") || w.endsWith("ical") || w.endsWith("ivo") || w.endsWith("iva"))
        ) {
            score += 1
        }
        if (w.any { it.isDigit() }) score += 1
        return score
    }

    /** Dos tokens están "pegados" si solo hay espacios entre ellos (sin comas, puntos, etc.). */
    private fun adjacent(text: String, a: Token, b: Token): Boolean {
        if (b.start < a.end) return false
        val gap = text.substring(a.end, b.start)
        if (gap.isEmpty()) return false
        for (c in gap) if (!c.isWhitespace() || c == '\n') return false
        return true
    }

    private fun isContent(t: Token): Boolean = t.pos == Pos.NOUN || t.pos == Pos.ADJ

    fun extract(text: String, tokens: List<Token>): Pair<List<Entry>, List<Span>> {
        val candidates = ArrayList<Candidate>()
        var i = 0
        val n = tokens.size
        while (i < n) {
            val first = tokens[i]
            if (first.pos != Pos.NOUN) {
                // Un adjetivo técnico suelto también puede ser término; se trata más abajo.
                i++
                continue
            }
            var last = i
            var words = 1
            var tech = technicalness(first)
            var proper = first.proper
            var j = i + 1
            while (j < n && words < 4) {
                val cur = tokens[j]
                val prev = tokens[j - 1]
                if (cur.sentenceStart || !adjacent(text, prev, cur)) break
                if (cur.pos == Pos.ADJ || (cur.pos == Pos.NOUN && cur.proper && first.proper)) {
                    // Sustantivo + adjetivo, o varios nombres propios seguidos.
                    last = j
                    words++
                    tech += technicalness(cur)
                    if (cur.proper) proper = true
                    j++
                    continue
                }
                if (linkWords.contains(cur.lower) || cur.pos == Pos.FUNC && linkWords.contains(cur.lower)) {
                    var k = j + 1
                    if (k < n && linkDeterminers.contains(tokens[k].lower) && adjacent(text, cur, tokens[k])) {
                        k++
                    }
                    if (k < n && tokens[k].pos == Pos.NOUN && !tokens[k].sentenceStart &&
                        adjacent(text, tokens[k - 1], tokens[k])
                    ) {
                        last = k
                        words++
                        tech += technicalness(tokens[k])
                        if (tokens[k].proper) proper = true
                        j = k + 1
                        continue
                    }
                }
                break
            }
            if (words >= 2) {
                val s = tokens[i].start
                val e = tokens[last].end
                val display = text.substring(s, e).replace(Regex("\\s+"), " ")
                candidates.add(Candidate(display.lowercase(), display, s, e, words, tech, proper))
            } else {
                val w = first.lower
                if (first.acronym || tech >= 1) {
                    candidates.add(Candidate(w, if (first.acronym) first.text else w, first.start, first.end, 1, tech, first.proper))
                } else if (first.proper) {
                    candidates.add(Candidate(w, first.text, first.start, first.end, 1, 0, true))
                }
            }
            i = last + 1
        }

        // Adjetivos técnicos sueltos (p. ej. "algorítmico").
        for (t in tokens) {
            if (t.pos == Pos.ADJ) {
                val tech = technicalness(t)
                if (tech >= 2) {
                    candidates.add(Candidate(t.lower, t.lower, t.start, t.end, 1, tech, false))
                }
            }
        }

        // Agrupar por clave y puntuar.
        class Acc(var display: String, var count: Int, val words: Int, val technical: Int, val proper: Boolean)
        val acc = LinkedHashMap<String, Acc>()
        val spans = ArrayList<Span>()
        val total = tokens.size.coerceAtLeast(1)
        // Se acepta más fácilmente en textos cortos, donde casi nada se repite.
        for (c in candidates) {
            val existing = acc[c.key]
            if (existing == null) {
                acc[c.key] = Acc(c.display, 1, c.words, c.technical, c.proper)
            } else {
                existing.count += 1
            }
        }
        val accepted = HashSet<String>()
        val entries = ArrayList<Entry>()
        for ((key, a) in acc) {
            val keep = when {
                a.count >= 2 -> true
                a.words >= 2 && (a.technical >= 1 || a.proper) -> true
                a.words >= 2 && total <= 60 -> true
                a.words == 1 && a.technical >= 2 -> true
                a.words == 1 && a.technical >= 1 && !a.proper -> true
                else -> false
            }
            if (!keep) continue
            accepted.add(key)
            val score = a.count * 2.0 + (a.words - 1) * 1.5 + a.technical * 2.0
            entries.add(Entry(a.display, Category.TERM, a.count, emptyList(), score))
        }
        for (c in candidates) {
            if (accepted.contains(c.key)) spans.add(Span(c.start, c.end, Category.TERM))
        }
        entries.sortWith(compareByDescending<Entry> { it.score }.thenBy { it.lemma.lowercase() })
        return Pair(entries.take(80), spans)
    }
}
