package com.extractor.gramatica.nlp

/** Etiquetador gramatical del español basado en léxico + sufijos + contexto. Funciona sin conexión. */
object SpanishTagger : Tagger {

    private val lex = SpanishLexicon

    private val strongDet: Set<String> = hashSetOf(
        "el", "la", "los", "las", "un", "una", "unos", "unas", "este", "esta", "estos", "estas",
        "ese", "esa", "esos", "esas", "aquel", "aquella", "aquellos", "aquellas", "mi", "mis",
        "tu", "tus", "su", "sus", "nuestro", "nuestra", "nuestros", "nuestras", "vuestro",
        "vuestra", "vuestros", "vuestras", "cada", "algún", "ningún", "cualquier", "del"
    )

    private val copulas: Set<String> = hashSetOf(
        "es", "son", "era", "eran", "fue", "fueron", "está", "están", "estaba", "estaban",
        "estuvo", "sea", "sean", "ser", "estar", "parece", "parecen", "resulta", "resultan",
        "queda", "quedan", "sigue", "siguen", "fuera", "siendo", "será", "serán", "sería",
        "serían", "estará", "estarán", "estaría", "estarían", "resultó", "parecía", "parecían",
        "permanece", "permanecen", "somos", "soy", "eres", "estoy", "estamos"
    )

    private val auxiliaries: Set<String> = hashSetOf(
        "he", "has", "ha", "hemos", "habéis", "han", "había", "habías", "habíamos", "habían",
        "hube", "hubo", "hubieron", "habré", "habrá", "habrán", "habría", "habrían", "haya",
        "hayan", "hubiera", "hubieran", "habiendo"
    )

    private val verbalClitics: Set<String> = hashSetOf(
        "me", "te", "se", "nos", "le", "les", "no", "yo", "tú", "él", "ella", "nosotros",
        "ellos", "ellas", "usted", "ustedes"
    )

    private val clitics = listOf("se", "lo", "la", "le", "me", "te", "los", "las", "les", "nos")

    // ---------------------------------------------------------------- utilidades

    private fun endsWithAny(w: String, vararg suffixes: String): Boolean {
        for (s in suffixes) if (w.endsWith(s)) return true
        return false
    }

    private fun isNumeric(w: String): Boolean {
        var digits = 0
        for (c in w) if (c.isDigit()) digits++
        return digits > 0 && digits * 2 >= w.length
    }

    private fun adjCandidates(w: String): List<String> {
        val out = ArrayList<String>(5)
        out.add(w)
        if (w.endsWith("es") && w.length > 4) out.add(w.dropLast(2))
        var b = w
        if (b.endsWith("s") && b.length > 3) {
            b = b.dropLast(1)
            out.add(b)
        }
        if (b.endsWith("a") && b.length > 2) out.add(b.dropLast(1) + "o")
        if (b.endsWith("ces") || w.endsWith("ces")) out.add(w.dropLast(3) + "z")
        return out
    }

    private fun knownAdjBase(w: String): String? {
        for (c in adjCandidates(w)) if (lex.adjectives.contains(c)) return c
        return null
    }

    private fun adjSure(w: String): Boolean {
        if (endsWithAny(w, "oso", "osa", "osos", "osas", "ísimo", "ísima", "ísimos", "ísimas")) return true
        if (endsWithAny(w, "able", "ible", "ables", "ibles") && !lex.nounExceptions.contains(w)) return true
        return false
    }

    private fun adjMaybe(w: String): Boolean {
        if (lex.adjectiveAr.contains(w)) return true
        return endsWithAny(
            w, "ico", "ica", "icos", "icas", "ivo", "iva", "ivos", "ivas", "al", "ales", "ante",
            "antes", "ente", "entes", "ario", "aria", "arios", "arias", "ial", "iales", "ista",
            "istas"
        ) && w.length > 4
    }

    private fun nounSure(w: String): Boolean {
        return w.length > 4 && endsWithAny(
            w, "ción", "ciones", "sión", "siones", "ión", "iones", "dad", "dades", "tad", "tades",
            "miento", "mientos", "aje", "ajes", "ura", "uras", "eza", "ezas", "ismo", "ismos",
            "ería", "erías", "logía", "logías", "grafía", "metría", "tud", "tudes", "encia",
            "ancia", "encias", "ancias", "umbre", "ez", "eces", "ería", "ador", "adores",
            "idor", "idores", "ario", "arios"
        ) && !endsWithAny(w, "ario", "arios") // -ario se decide por contexto (adjetivo o sustantivo)
    }

    private fun verbSure(w: String): Boolean {
        if (w.length >= 6 && endsWithAny(w, "ando", "iendo", "yendo", "aba", "abas", "aban", "ábamos",
                "aron", "ieron", "arán", "erán", "irán", "aría", "ería", "iría", "arían", "erían", "irían")) {
            return true
        }
        return w.length >= 5 && w.endsWith("ó")
    }

    private fun isInfinitiveShape(w: String): Boolean {
        if (w.length < 5) return false
        if (!endsWithAny(w, "ar", "er", "ir", "ír")) return false
        if (lex.nounExceptions.contains(w) || lex.adjectiveAr.contains(w)) return false
        return true
    }

    private fun cliticInfinitive(w: String): String? {
        for (c in clitics) {
            if (w.length > c.length + 3 && w.endsWith(c)) {
                val r = w.dropLast(c.length)
                val known = VerbForms.lemmaOf[r]
                if (known != null && known == r) return r
                if (c == "se" && r.length >= 4 && endsWithAny(r, "ar", "er", "ir")) return r
            }
        }
        return null
    }

    // ---------------------------------------------------------------- pasada 1: léxico

    private fun pass1(t: Token) {
        val w = t.lower
        if (isNumeric(w)) {
            t.pos = Pos.NUM
            t.certain = true
            return
        }
        if (t.acronym) {
            t.pos = Pos.NOUN
            t.proper = true
            t.certain = true
            return
        }
        if (lex.stopwords.contains(w)) {
            t.pos = Pos.FUNC
            t.certain = true
            return
        }
        if (lex.adverbs.contains(w) || (w.length > 7 && w.endsWith("mente"))) {
            t.pos = Pos.ADV
            t.certain = true
            return
        }
        val verb = VerbForms.lemmaOf[w]
        if (verb != null) {
            t.pos = Pos.VERB
            t.lemma = verb
            t.certain = true
            return
        }
        val adj = knownAdjBase(w)
        if (adj != null) {
            t.pos = Pos.ADJ
            t.lemma = adj
            t.certain = true
            return
        }
        if (lex.nounExceptions.contains(w)) {
            t.pos = Pos.NOUN
            t.certain = true
            return
        }
        val clit = cliticInfinitive(w)
        if (clit != null) {
            t.pos = Pos.VERB
            t.lemma = VerbForms.lemmaOf[clit] ?: clit
            t.certain = true
            return
        }
        if (adjSure(w)) {
            t.pos = Pos.ADJ
            t.certain = true
            return
        }
        if (nounSure(w)) {
            t.pos = Pos.NOUN
            t.certain = true
            return
        }
        if (verbSure(w) || isInfinitiveShape(w)) {
            t.pos = Pos.VERB
            t.certain = true
            return
        }
        // Valor por defecto: sustantivo provisional (el contexto puede cambiarlo).
        t.pos = Pos.NOUN
        t.certain = false
    }

    // ---------------------------------------------------------------- pasada 2: contexto

    private fun pass2(tokens: List<Token>, i: Int) {
        val t = tokens[i]
        if (t.pos == Pos.FUNC || t.pos == Pos.NUM || t.pos == Pos.OTHER) return
        val w = t.lower
        val prev: Token? = if (i > 0 && !t.sentenceStart) tokens[i - 1] else null
        val next: Token? = if (i + 1 < tokens.size && !tokens[i + 1].sentenceStart) tokens[i + 1] else null

        val prevStrongDet = prev != null && strongDet.contains(prev.lower)
        val prevCopula = prev != null && copulas.contains(prev.lower)
        val prevAux = prev != null && auxiliaries.contains(prev.lower)
        val prevDegree = prev != null && lex.degreeAdverbs.contains(prev.lower)
        val prevNoun = prev != null && prev.pos == Pos.NOUN

        // Mayúscula en mitad de oración: nombre propio.
        if (t.pos == Pos.NOUN && !t.proper && t.allowProper && t.text.length > 1 &&
            t.text[0].isUpperCase() && !t.sentenceStart) {
            t.proper = true
            t.certain = true
            return
        }

        // Formas verbales que funcionan como sustantivo tras un determinante: "el trabajo", "la cuenta".
        if (t.pos == Pos.VERB && prevStrongDet && !copulas.contains(w) && !auxiliaries.contains(w)) {
            t.pos = Pos.NOUN
            t.lemma = w
            return
        }

        // Participios tras sustantivo, cópula o adverbio de grado: funcionan como adjetivos.
        if (t.pos == Pos.VERB && VerbForms.isParticiple(w) && !prevAux) {
            if (prevNoun || prevCopula || prevDegree) {
                t.pos = Pos.ADJ
                t.lemma = participleBase(w)
                return
            }
        }

        // "mejor", "peor", "mayor"... como adjetivo cuando acompañan a un sustantivo.
        if (t.pos == Pos.ADV && knownAdjBase(w) != null && (prevStrongDet || prevNoun) &&
            next != null && next.pos != Pos.FUNC && next.pos != Pos.VERB) {
            t.pos = Pos.ADJ
            t.lemma = knownAdjBase(w) ?: w
            return
        }

        // Adjetivos que también son sustantivos: "el manual de ...", "el general".
        if (t.pos == Pos.ADJ && lex.nounExceptions.contains(w) && prevStrongDet &&
            (next == null || (next.pos != Pos.NOUN && next.pos != Pos.ADJ))) {
            t.pos = Pos.NOUN
            t.lemma = w
            return
        }

        if (t.certain) return

        // Sustantivo provisional: aplicar contexto.
        if (t.pos == Pos.NOUN) {
            if (prevStrongDet) return
            if (VerbForms.isParticiple(w)) {
                when {
                    prevAux -> t.pos = Pos.VERB
                    prevCopula || prevDegree || prevNoun -> t.pos = Pos.ADJ
                }
                return
            }
            if (adjMaybe(w) && (prevNoun || prevCopula || prevDegree)) {
                t.pos = Pos.ADJ
                return
            }
            if (prevDegree) {
                t.pos = Pos.ADJ
                return
            }
            if (prev != null && verbalClitics.contains(prev.lower) && w.length >= 3 &&
                endsWithAny(w, "a", "e", "an", "en", "o", "as", "es", "ía", "ían", "amos", "emos",
                    "imos", "ará", "erá", "irá")) {
                t.pos = Pos.VERB
                return
            }
        }
    }

    private fun participleBase(w: String): String {
        var b = w
        if (b.endsWith("s")) b = b.dropLast(1)
        if (b.endsWith("a")) b = b.dropLast(1) + "o"
        return b
    }

    override fun tag(tokens: List<Token>) {
        for (t in tokens) pass1(t)
        for (i in tokens.indices) pass2(tokens, i)
    }
}
