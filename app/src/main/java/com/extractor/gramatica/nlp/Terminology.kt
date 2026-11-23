package com.extractor.gramatica.nlp

/**
 * Extrae terminología: sintagmas nominales técnicos (sustantivo + adjetivo, sustantivo + "de" + sustantivo),
 * siglas, marcas/nombres con mayúsculas internas y palabras de origen científico o extranjero.
 */
object Terminology {

    private fun ws(s: String): HashSet<String> = s.split(' ').filter { it.isNotBlank() }.toHashSet()

    /** Sustantivos demasiado genéricos para ser términos (se descartan al inicio de una frase). */
    private val generic: HashSet<String> = HashSet<String>().apply {
        addAll(SpanishVocab.genericNouns)
        addAll(
            ws(
                "rama ramas investigador investigadores científico científicos autor autores experto " +
                    "expertos persona personas parte partes tipo tipos clase clases serie conjunto " +
                    "volumen volúmenes cantidad cantidades número resto mayoría minoría ejemplo " +
                    "forma formas manera maneras proceso procesos tasa nivel niveles fase etapa " +
                    "thing things way ways lot lots kind kinds type types part parts number case cases " +
                    "point points time times year years people group groups set sets series piece " +
                    "researcher researchers author authors expert experts person persons"
            )
        )
    }

    /** Adjetivos demasiado generales para formar un término. */
    private val weakAdjectives: HashSet<String> = ws(
        "grande grandes gran nuevo nueva nuevos nuevas mejor mejores mayor mayores menor menores mismo " +
            "misma mismos mismas otro otra otros otras buen bueno buena buenos buenas mal malo mala " +
            "importante importantes principal principales diferente diferentes distinto distinta " +
            "distintos distintas posible posibles necesario necesaria necesarios necesarias general " +
            "generales diverso diversa diversos diversas varios varias numeroso numerosa numerosos " +
            "numerosas último última últimos últimas primero primera primeros primeras siguiente " +
            "siguientes anterior anteriores actual actuales propio propia propios propias cierto " +
            "cierta ciertos ciertas mucho mucha muchos muchas poco poca pocos pocas simple simples " +
            "sencillo sencilla fácil fáciles difícil difíciles rápido rápida lento lenta bajo baja " +
            "alto alta largo larga corto corta pequeño pequeña pequeños pequeñas completo completa " +
            "presente presentes total real reales ambos ambas todo toda todos todas cada demás tal tales " +
            "new old big small large little long short high low good bad best better main major minor " +
            "different possible same other many few several various certain whole simple easy hard"
    )

    /** Sustantivos que suelen encabezar términos técnicos ("base de datos", "red neuronal"). */
    private val termHeads: HashSet<String> = ws(
        "base red sistema modelo lenguaje aprendizaje análisis algoritmo teoría ley método función " +
            "ecuación inteligencia energía célula células proteína enzima ácido molécula átomo reacción " +
            "ciclo campo fuerza onda señal circuito procesador memoria servidor protocolo interfaz " +
            "arquitectura framework red sistema clima ecosistema especie población gen genoma virus " +
            "bacteria tejido órgano sistema enfermedad síndrome tratamiento diagnóstico derecho contrato " +
            "norma sentencia delito mercado inflación interés capital inversión " +
            "network system model language learning analysis algorithm theory law method function " +
            "equation intelligence energy cell protein enzyme acid molecule atom reaction cycle field " +
            "force wave signal circuit processor memory server protocol interface architecture"
    )

    private val links = ws("de del of")
    private val dets = ws("la el los las un una the")

    private val sciencePrefixes = listOf(
        "bio", "geo", "hidro", "termo", "micro", "macro", "neuro", "psico", "foto", "electro", "eléctro",
        "nano", "cardio", "fisio", "cito", "cloro", "mito", "quimio", "astro", "aero", "ciber", "gastro",
        "hemo", "osteo", "dermo", "endo", "exo", "hiper", "hipo", "algorit", "cripto", "crypto", "cyber",
        "hydro", "thermo", "photo", "genom", "proteo", "metabol"
    )
    private val scienceSuffixes = listOf(
        "logía", "ología", "grafía", "metría", "scopía", "patía", "terapia", "cracia", "itis", "osis",
        "ology", "ography", "metry", "plasma", "ización", "ization", "ómetro", "ometer"
    )
    private val foreignEndings = listOf("ware", "ing", "work", "chain", "base", "net")

    private class Cand(
        val key: String,
        val display: String,
        val start: Int,
        val end: Int,
        val words: Int,
        val tech: Int,
        val proper: Boolean,
        val single: Boolean,
        val plausible: Boolean = false
    )

    private fun isGeneric(t: Token): Boolean = generic.contains(t.lower)

    /** Pista de que una palabra suelta es un término (origen científico o extranjero). */
    private fun techSingle(t: Token): Int {
        val w = t.lower
        var score = 0
        if (w.any { it.isDigit() }) score++
        if (w.length >= 8) {
            if (sciencePrefixes.any { w.startsWith(it) }) score++
            else if (scienceSuffixes.any { w.endsWith(it) && w.length >= it.length + 3 }) score++
            else if (w.endsWith("sis") && w.length >= 9) score++
        }
        if (w.length >= 4 && (w.contains('w') || w.contains('k'))) score++
        else if (w.length >= 7 && foreignEndings.any { w.endsWith(it) }) score++
        else if (w.length >= 5 && (w.contains("ph") || w.contains("th") || w.contains("ck") || w.contains("sh"))) score++
        return score
    }

    /** Pista de que un sustantivo o adjetivo dentro de una frase es técnico. */
    private fun techInPhrase(t: Token): Int {
        var score = techSingle(t)
        val w = t.lower
        if (t.acronym) score += 2
        if (w.length >= 6 && (w.endsWith("ico") || w.endsWith("ica") || w.endsWith("icos") || w.endsWith("icas") ||
                w.endsWith("ivo") || w.endsWith("iva") || w.endsWith("ivos") || w.endsWith("ivas") ||
                w.endsWith("ial") || w.endsWith("iales") || w.endsWith("ical"))
        ) {
            score++
        }
        return score
    }

    private fun mixedCase(w: String): Boolean {
        if (w.length < 3) return false
        var i = 1
        while (i < w.length) {
            if (w[i].isUpperCase()) return true
            i++
        }
        return false
    }

    /** Dos tokens están "pegados" si solo hay espacios entre ellos (sin comas, puntos, etc.). */
    private fun adjacent(text: String, a: Token, b: Token): Boolean {
        if (b.start < a.end) return false
        val gap = text.substring(a.end, b.start)
        if (gap.isEmpty()) return false
        for (c in gap) if (!c.isWhitespace() || c == '\n') return false
        return true
    }

    fun extract(text: String, tokens: List<Token>): Pair<List<Entry>, List<Span>> {
        val cands = ArrayList<Cand>()
        val n = tokens.size
        var i = 0
        while (i < n) {
            val first = tokens[i]
            if (first.pos != Pos.NOUN) {
                i++
                continue
            }
            // Cabeza genérica ("tipo de", "conjunto de", "rama de la"): se salta y se empieza tras el enlace.
            if (isGeneric(first)) {
                val nx = if (i + 1 < n) tokens[i + 1] else null
                if (nx != null && !nx.sentenceStart && links.contains(nx.lower) && adjacent(text, first, nx)) {
                    var k = i + 2
                    if (k < n && dets.contains(tokens[k].lower) && adjacent(text, tokens[k - 1], tokens[k])) k++
                    i = k
                } else {
                    i++
                }
                continue
            }

            var last = i
            var minLen = first.lower.length
            var maxLen = first.lower.length
            var content = 1
            var tech = techInPhrase(first)
            var proper = first.proper
            var linked = false
            var j = i + 1
            while (j < n && content < 3) {
                val cur = tokens[j]
                val prev = tokens[j - 1]
                if (cur.sentenceStart || !adjacent(text, prev, cur)) break
                if (cur.pos == Pos.ADJ && !weakAdjectives.contains(cur.lower) && !weakAdjectives.contains(cur.lemma) &&
                    (!VerbForms.isParticiple(cur.lower) || SpanishTagger.isKnownAdjective(cur.lower))) {
                    last = j
                    content++
                    tech += techInPhrase(cur)
                    minLen = minOf(minLen, cur.lower.length)
                    maxLen = maxOf(maxLen, cur.lower.length)
                    j++
                    continue
                }
                if (cur.pos == Pos.NOUN && cur.proper && first.proper) {
                    last = j
                    content++
                    j++
                    continue
                }
                if (!linked && links.contains(cur.lower)) {
                    var k = j + 1
                    if (k < n && dets.contains(tokens[k].lower) && adjacent(text, cur, tokens[k])) k++
                    if (k < n && tokens[k].pos == Pos.NOUN && !tokens[k].sentenceStart && !isGeneric(tokens[k]) &&
                        adjacent(text, tokens[k - 1], tokens[k])
                    ) {
                        last = k
                        content++
                        minLen = minOf(minLen, tokens[k].lower.length)
                        maxLen = maxOf(maxLen, tokens[k].lower.length)
                        linked = true
                        tech += techInPhrase(tokens[k])
                        if (tokens[k].proper) proper = true
                        j = k + 1
                        continue
                    }
                }
                break
            }

            val s = tokens[i].start
            val e = tokens[last].end
            if (content >= 2) {
                val raw = text.substring(s, e).replace(Regex("\\s+"), " ")
                val keepCase = proper || tokens.subList(i, last + 1).any { it.acronym }
                val display = if (keepCase) raw else raw.lowercase()
                val plausible = termHeads.contains(first.lower) || minLen >= 7 || (maxLen >= 10 && minLen >= 5)
                cands.add(Cand(raw.lowercase(), display, s, e, content, tech, proper, false, plausible))
            } else {
                // Palabra suelta: solo siglas, marcas con mayúsculas internas, o palabras de origen técnico.
                val w = first.text
                when {
                    first.acronym -> cands.add(Cand(first.lower, first.text, s, e, 1, 2, true, true))
                    mixedCase(w) -> cands.add(Cand(first.lower, w, s, e, 1, 2, true, true))
                    first.proper -> cands.add(Cand(first.lower, w, s, e, 1, 0, true, true))
                    techSingle(first) >= 1 -> cands.add(Cand(first.lower, first.lower, s, e, 1, techSingle(first), false, true))
                }
            }
            i = last + 1
        }

        // Agrupar por clave.
        class Acc(
            val display: String, var count: Int, val words: Int, val tech: Int, val proper: Boolean,
            val single: Boolean, val plausible: Boolean
        )
        val acc = LinkedHashMap<String, Acc>()
        for (c in cands) {
            val ex = acc[c.key]
            if (ex == null) acc[c.key] = Acc(c.display, 1, c.words, c.tech, c.proper, c.single, c.plausible) else ex.count += 1
        }

        val accepted = HashSet<String>()
        for ((key, a) in acc) {
            val keep = if (a.single) {
                // Los nombres propios sueltos solo cuentan si se repiten; siglas y términos técnicos siempre.
                a.tech >= 1 || a.count >= 2
            } else {
                a.count >= 2 || a.tech >= 1 || a.proper || a.plausible
            }
            if (keep) accepted.add(key)
        }

        // Una palabra suelta que solo aparece dentro de términos más largos ya aceptados se descarta.
        val multi = acc.filter { accepted.contains(it.key) && !it.value.single }
        val finalKeys = ArrayList<String>()
        for (key in accepted) {
            val a = acc.getValue(key)
            if (a.single && !a.proper) {
                val inside = multi.entries.filter { (k, _) -> k != key && Regex("(^|\\s)" + Regex.escape(key) + "(\\s|$)").containsMatchIn(k) }
                    .sumOf { it.value.count }
                if (inside >= a.count) continue
            }
            finalKeys.add(key)
        }

        val entries = ArrayList<Entry>()
        for (key in finalKeys) {
            val a = acc.getValue(key)
            val score = a.count * 2.0 + (a.words - 1) * 1.5 + a.tech * 2.0 + (if (a.proper) 1.0 else 0.0)
            entries.add(Entry(a.display, Category.TERM, a.count, emptyList(), score))
        }
        entries.sortWith(compareByDescending<Entry> { it.score }.thenBy { it.lemma.lowercase() })
        val kept = entries.take(80)
        val keptKeys = kept.map { it.lemma.lowercase() }.toHashSet()
        val spans = ArrayList<Span>()
        for (c in cands) if (c.key in keptKeys) spans.add(Span(c.start, c.end, Category.TERM))
        return Pair(kept, spans)
    }
}
