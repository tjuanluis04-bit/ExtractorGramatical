package com.extractor.gramatica.nlp

/**
 * Etiquetador gramatical del español sin conexión.
 *
 * Cada palabra recibe una puntuación por clase (sustantivo, adjetivo, verbo, adverbio) a partir del léxico,
 * los sufijos y la conjugación; después se ajustan con el contexto (vecinos) en varias pasadas.
 */
object SpanishTagger : Tagger {

    private const val N = 0
    private const val A = 1
    private const val V = 2
    private const val R = 3
    private const val F = 4
    private const val NUM = 5

    private val lex = SpanishLexicon
    private val DEBUG = System.getenv("TAGDEBUG") != null

    private fun ws(s: String): HashSet<String> = s.split(' ').filter { it.isNotBlank() }.toHashSet()

    // ------------------------------------------------------------------ léxico

    private val adjSet: HashSet<String> = HashSet<String>().apply {
        addAll(lex.adjectives)
        addAll(SpanishVocab.adjectives)
    }
    private val advSet: HashSet<String> = HashSet<String>().apply {
        addAll(lex.adverbs)
        addAll(SpanishVocab.adverbs)
    }
    private val nounFirst: HashSet<String> = SpanishVocab.nounFirst.toHashSet()
    private val nounExc: HashSet<String> = lex.nounExceptions.toHashSet()

    private const val numberWords =
        "uno dos tres cuatro cinco seis siete ocho nueve diez once doce trece catorce quince " +
            "veinte treinta cuarenta cincuenta sesenta setenta ochenta noventa cien ciento mil " +
            "millón millones doscientos trescientos quinientos"

    /** Palabras funcionales: nunca se extraen. */
    private val funcWords: HashSet<String> = HashSet<String>().apply {
        addAll(lex.determiners)
        addAll(lex.pronouns)
        addAll(lex.prepositions)
        addAll(lex.conjunctions)
        addAll(ws("no sí ni etc tal tales cual cuales"))
        addAll(ws(numberWords))
        // Los adverbios de lugar/tiempo/modo (aquí, ahora, siempre…) sí se extraen como adverbios.
        removeAll(setOf("mañana", "sólo", "solo", "ya", "luego", "además", "entonces"))
    }

    /** Cuantificadores que a veces funcionan como adverbio ("trabaja mucho", "demasiado grande"). */
    private val quantAdv = ws("mucho poco bastante demasiado tanto")

    private val strongDet = ws(
        "el la los las un una unos unas al del este esta estos estas ese esa esos esas aquel aquella " +
            "aquellos aquellas mi mis tu tus su sus nuestro nuestra nuestros nuestras vuestro vuestra " +
            "vuestros vuestras cada algún ningún cualquier"
    )
    private val quant = ws(
        "mucho mucha muchos muchas poco poca pocos pocas varios varias algunos algunas alguno alguna " +
            "otros otras otro otra todos todas todo toda ambos ambas tantos tantas ciertos ciertas " +
            "demás sendos sendas numerosos numerosas diversos diversas"
    )
    private val prepSet = ws(
        "a ante bajo con contra de desde durante en entre hacia hasta mediante para por según sin " +
            "sobre tras"
    )
    private val serForms = ws(
        "es son era eran fue fueron será serán sería serían sea sean fuera fueran ser siendo sido soy " +
            "eres somos eras fui fuiste fuimos"
    )
    private val estarForms = ws(
        "está están estaba estaban estuvo estuvieron estará estarán estaría estarían esté estén " +
            "estar estando estado estoy estás estamos estás estuve estuviste estuvimos"
    )
    private val otherCopulas = ws(
        "parece parecen parecía parecían resulta resultan resultó resultaba quedó queda quedan " +
            "permanece permanecen sigue siguen seguía continúa continúan"
    )
    private val auxHaber = ws(
        "he has ha hemos habéis han había habías habíamos habían hube hubo hubieron habré habrá " +
            "habrán habría habrían haya hayan hubiera hubieran habiendo haber"
    )
    private val preVerbal = ws(
        "se me te nos os le les no yo tú él ella usted nosotros nosotras ellos ellas ustedes ya " +
            "también tampoco nunca jamás siempre que quien quienes"
    )
    private val degreeAdv = ws(
        "muy más menos tan bastante demasiado sumamente extremadamente poco casi " +
            "realmente"
    )
    private val numSet = ws(numberWords)
    private val partAdv = ws("recién bien mal ya aún todavía apenas casi")
    private val langs = ws(
        "español inglés francés alemán italiano portugués chino ruso japonés árabe catalán latín " +
            "griego coreano euskera gallego hindi"
    )
    private val strictDegree = ws("muy tan sumamente extremadamente casi")
    private val conj = ws("y e o u ni pero sino")
    private val notGerund = ws("comando comandos mando mandos bando bandos")
    private val ordinalAdj = ws(
        "primero primer segundo tercero tercer último siguiente anterior mismo propio mayor menor " +
            "mejor peor único varios cierto"
    )

    // ------------------------------------------------------------------ utilidades

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
        if (w.endsWith("ces")) out.add(w.dropLast(3) + "z")
        return out
    }

    private fun knownAdjBase(w: String): String? {
        for (c in adjCandidates(w)) if (adjSet.contains(c)) return c
        return null
    }

    private fun isNounExc(w: String): Boolean {
        if (nounExc.contains(w)) return true
        if (w.endsWith("es") && w.length > 4 && nounExc.contains(w.dropLast(2))) return true
        if (w.endsWith("s") && w.length > 3 && nounExc.contains(w.dropLast(1))) return true
        return false
    }

    private fun adjSuffixStrong(w: String): Boolean {
        if (w.length < 5) return false
        if (endsWithAny(w, "oso", "osa", "osos", "osas", "ísimo", "ísima", "ísimos", "ísimas")) return true
        if (endsWithAny(w, "able", "ible", "ables", "ibles") && !isNounExc(w)) return true
        return false
    }

    private fun adjSuffixWeak(w: String): Boolean {
        if (w.length < 5) return false
        if (lex.adjectiveAr.contains(w)) return true
        return endsWithAny(
            w, "ico", "ica", "icos", "icas", "ivo", "iva", "ivos", "ivas", "ales", "ante", "antes",
            "ente", "entes", "iales", "ista", "istas", "ense", "enses", "ario", "aria", "arios", "arias",
            "eño", "eña", "eños", "eñas", "ol", "oles"
        ) || ((w.endsWith("al") || w.endsWith("ales")) && !isNounExc(w))
    }

    /** ¿Es un adjetivo conocido del léxico (incluye flexiones)? */
    fun isKnownAdjective(w: String): Boolean = knownAdjBase(w) != null

    private fun looksAdj(w: String): Boolean = knownAdjBase(w) != null || adjSuffixStrong(w) || adjSuffixWeak(w)

    /** ¿Concuerdan en número y terminación dos palabras (sustantivo + adjetivo)? */
    private fun agrees(noun: String, adj: String): Boolean {
        fun num(x: String) = if (x.endsWith("s")) 1 else 0
        fun gen(x: String): Int {
            val y = x.removeSuffix("s").removeSuffix("e")
            return when {
                y.endsWith("a") -> 1
                y.endsWith("o") -> 2
                else -> 0
            }
        }
        if (num(noun) != num(adj)) return false
        if (!(adj.endsWith("o") || adj.endsWith("os") || adj.endsWith("a") || adj.endsWith("as") ||
                adj.endsWith("es"))) return false
        val gn = gen(noun)
        val ga = gen(adj)
        return ga == 0 || gn == 0 || gn == ga
    }

    private fun nounSuffix(w: String): Boolean {
        if (w.length < 5) return false
        return endsWithAny(
            w, "ción", "ciones", "sión", "siones", "dad", "dades", "tad", "tades", "miento", "mientos",
            "aje", "ajes", "ura", "uras", "eza", "ezas", "ismo", "ismos", "ería", "erías", "logía",
            "logías", "grafía", "metría", "tud", "tudes", "umbre", "idor", "adores", "idores", "ncia",
            "ncias", "ez", "eces", "triz", "trices"
        ) || (w.length > 6 && w.endsWith("ador"))
    }

    private fun shapeInfinitive(w: String): Boolean {
        if (w.length < 6) return false
        if (!endsWithAny(w, "ar", "er", "ir", "ír")) return false
        if (nounExc.contains(w) || lex.adjectiveAr.contains(w)) return false
        return true
    }

    private fun sureVerbSuffix(w: String): Boolean {
        if (w.length < 6 || notGerund.contains(w)) return false
        if (endsWithAny(w, "ando", "iendo", "yendo", "aron", "ieron", "aban", "ábamos", "arán",
                "erán", "irán", "arían", "erían", "irían", "ían", "ará", "erá", "irá", "aría")) {
            return true
        }
        if (w.length >= 5 && w.endsWith("ó") && !isNounExc(w)) return true
        return w.length >= 7 && endsWithAny(w, "aba", "abas")
    }

    private fun guessInfinitive(w: String): String {
        fun st(n: Int) = w.dropLast(n)
        return when {
            w.endsWith("ando") -> st(4) + "ar"
            w.endsWith("iendo") -> st(5) + "er"
            w.endsWith("yendo") -> st(5) + "er"
            w.endsWith("ábamos") -> st(6) + "ar"
            w.endsWith("aron") -> st(4) + "ar"
            w.endsWith("aban") -> st(4) + "ar"
            w.endsWith("abas") -> st(4) + "ar"
            w.endsWith("aba") -> st(3) + "ar"
            w.endsWith("arán") || w.endsWith("arían") -> w.substringBefore("ar", w) + "ar"
            w.endsWith("ará") -> st(2) + "r"
            w.endsWith("aría") -> st(3) + "r"
            w.endsWith("ieron") -> st(5) + "er"
            w.endsWith("erán") -> st(3)
            w.endsWith("erá") -> st(2) + "r"
            w.endsWith("ería") -> st(3) + "r"
            w.endsWith("erían") -> st(4) + "r"
            w.endsWith("irán") -> st(3)
            w.endsWith("irá") -> st(2) + "r"
            w.endsWith("iría") -> st(3) + "r"
            w.endsWith("irían") -> st(4) + "r"
            w.endsWith("ían") -> st(3) + "er"
            w.endsWith("ió") -> st(2) + "er"
            w.endsWith("ó") -> st(1) + "ar"
            else -> w
        }
    }

    private val clitics = listOf("se", "lo", "la", "le", "me", "te", "los", "las", "les", "nos")

    private fun cliticInfinitive(w: String): String? {
        for (c in clitics) {
            if (w.length > c.length + 4 && w.endsWith(c)) {
                val r = w.dropLast(c.length)
                if (!endsWithAny(r, "ar", "er", "ir", "ír")) continue
                if (VerbForms.infinitives.contains(r)) return r
            }
        }
        return null
    }

    // ------------------------------------------------------------------ puntuación inicial

    private fun init(t: Token, sc: DoubleArray, fixed: IntArray, i: Int) {
        val w = t.lower
        if (isNumeric(w)) {
            fixed[i] = NUM
            t.pos = Pos.NUM
            return
        }
        if (t.acronym) {
            fixed[i] = N
            t.proper = true
            return
        }
        if (funcWords.contains(w) && !quantAdv.contains(w)) {
            fixed[i] = F
            return
        }

        var known = false

        // Adverbios
        if (quantAdv.contains(w)) {
            sc[R] = 2.0
            known = true
        } else if (w.length > 7 && w.endsWith("mente")) {
            sc[R] = 4.0
            known = true
        } else if (advSet.contains(w)) {
            sc[R] = 3.4
            known = true
        }

        // Sustantivos léxicos
        if (isNounExc(w)) {
            sc[N] = 3.0
            known = true
        }
        if (nounFirst.contains(w)) {
            sc[N] = maxOf(sc[N], 3.0)
            known = true
        }

        // Adjetivos
        val adjBase = knownAdjBase(w)
        if (adjBase != null) {
            sc[A] = maxOf(sc[A], if (adjBase == "primer" || adjBase == "tercer" || adjBase == "gran" || adjBase == "buen") 3.4 else 3.0)
            sc[N] = maxOf(sc[N], 0.8)
            known = true
        } else if (adjSuffixStrong(w)) {
            sc[A] = 2.5
            sc[N] = maxOf(sc[N], 0.8)
            known = true
        } else if (adjSuffixWeak(w)) {
            sc[A] = maxOf(sc[A], 1.3)
            sc[N] = maxOf(sc[N], 1.5)
            known = true
        }

        // Sustantivos por sufijo
        if (nounSuffix(w)) {
            sc[N] = maxOf(sc[N], 2.8)
            known = true
        }

        // Verbos
        val lemma = VerbForms.lemmaOf[w]
        val isInf = VerbForms.infinitives.contains(w)
        if (lemma != null) {
            t.lemma = lemma
            val noun = sc[N] >= 3.0
            var v = 3.0
            if (isInf) v = 3.6
            else if (VerbForms.firstPerson.contains(w) || VerbForms.secondSg.contains(w)) v = 1.3
            else if (VerbForms.thirdSgA.contains(w)) v = 2.0
            if (VerbForms.isParticiple(w) && !isInf) {
                // participio: verbo (tiempos compuestos / pasiva) o adjetivo (según contexto)
                sc[V] = maxOf(sc[V], 2.0)
                sc[A] = maxOf(sc[A], 1.9)
            } else {
                sc[V] = maxOf(sc[V], if (noun) minOf(v, 1.5) else v)
            }
            if (VerbForms.firstPerson.contains(w) || VerbForms.thirdSgA.contains(w) ||
                VerbForms.secondSg.contains(w)) {
                sc[N] = maxOf(sc[N], 1.8)
            } else if (!isInf) {
                sc[N] = maxOf(sc[N], 1.0)
            }
            known = true
        } else if (sureVerbSuffix(w)) {
            sc[V] = maxOf(sc[V], 2.8)
            t.lemma = guessInfinitive(w)
            known = true
        } else {
            val clit = cliticInfinitive(w)
            if (clit != null) {
                sc[V] = 3.3
                t.lemma = clit
                known = true
            } else if (shapeInfinitive(w) && !known) {
                sc[V] = 2.4
                sc[N] = 0.8
                t.lemma = w
                known = true
            }
        }

        // Palabra desconocida: se supone sustantivo con alternativas débiles.
        if (!known) {
            sc[N] = 1.2
            sc[A] = 0.5
            sc[V] = 0.3
        }

        // Mayúscula en mitad de oración: nombre propio.
        if (t.allowProper && !t.sentenceStart && t.text.length > 1 && t.text[0].isUpperCase()) {
            fixed[i] = N
            t.proper = true
        }
    }

    // ------------------------------------------------------------------ contexto

    /** ¿Hay algún verbo en la misma oración aparte de la posición k? */
    private fun verbElsewhere(tokens: List<Token>, cls: IntArray, k: Int): Boolean {
        var j = k
        while (j > 0 && !tokens[j].sentenceStart && k - j < 25) {
            j--
            if (cls[j] == V) return true
        }
        j = k + 1
        while (j < tokens.size && !tokens[j].sentenceStart && j - k < 25) {
            if (cls[j] == V) return true
            j++
        }
        return false
    }

    private fun applyContext(tokens: List<Token>, i: Int, sc: DoubleArray, cls: IntArray) {
        val t = tokens[i]
        val w = t.lower
        val n = tokens.size
        val bothKnown = sc[R] >= 3.0 && sc[A] >= 3.0
        val lexAdverb = sc[R] >= 2.0
        val hasPrev = i > 0 && !t.sentenceStart
        val prev: Token? = if (hasPrev) tokens[i - 1] else null
        val prev2: Token? = if (hasPrev && i > 1 && !tokens[i - 1].sentenceStart) tokens[i - 2] else null
        val next: Token? = if (i + 1 < n && !tokens[i + 1].sentenceStart) tokens[i + 1] else null
        val pw = prev?.lower ?: ""
        val prevCls = if (prev != null) cls[i - 1] else -1
        val prev2Cls = if (prev2 != null) cls[i - 2] else -1
        val nextCls = if (next != null) cls[i + 1] else -1
        val soft = t.gap.any { it == ',' || it == ';' || it == '(' || it == ')' || it == '—' || it == '–' || it == '"' }

        val isInfinitive = VerbForms.infinitives.contains(w) || shapeInfinitive(w) ||
            cliticInfinitive(w) != null
        val isGerund = w.endsWith("ando") || w.endsWith("iendo") || w.endsWith("yendo")
        val isPart = VerbForms.isParticiple(w) && !isInfinitive
        val knownVerb = VerbForms.lemmaOf.containsKey(w) || sureVerbSuffix(w)
        val knownAdj = knownAdjBase(w) != null || adjSuffixStrong(w)
        val adjLike = knownAdj || adjSuffixWeak(w) || isPart
        val nextNounish = next != null && !looksAdj(next.lower) &&
            (nextCls == N || ((VerbForms.thirdSgA.contains(next.lower) ||
                VerbForms.firstPerson.contains(next.lower) || nounFirst.contains(next.lower)) &&
                verbElsewhere(tokens, cls, i + 1)))
        val endsAdjMorph = endsWithAny(w, "o", "a", "os", "as", "e", "es", "al", "ales", "ar", "ares", "or", "ores")

        // Sin ningún otro verbo en la oración, una forma verbal ambigua funciona como verbo.
        if (VerbForms.lemmaOf.containsKey(w) && !VerbForms.isParticiple(w) && sc[V] >= 1.0 &&
            !(next != null && (next.lower == "de" || next.lower == "del")) &&
            !(prev != null && (strongDet.contains(pw) || prepSet.contains(pw))) &&
            !verbElsewhere(tokens, cls, i)) {
            sc[V] += 2.0
        }

        // Cuantificadores que pueden ser adverbios ("trabaja mucho", "demasiado grande").
        if (quantAdv.contains(w)) {
            when {
                prevCls == V -> sc[R] += 2.5
                nextCls == A || nextCls == R || nextCls == V -> sc[R] += 2.5
                prev != null && strongDet.contains(pw) -> sc[N] += 2.0
                else -> sc[F] += 3.0
            }
            return
        }

        if (prev != null) {
            val afterStrongDet = strongDet.contains(pw)
            val afterQuant = quant.contains(pw)
            val afterPrep = prepSet.contains(pw)

            // Tras un determinante: no hay verbo conjugado.
            if (afterStrongDet) {
                if (!isPart && !isGerund) sc[V] -= 3.0 else sc[V] -= 1.0
                sc[R] -= 2.0
                sc[N] += 0.8
                // Adjetivo sustantivado: "los jóvenes", "lo importante".
                if ((knownAdj || isPart) && nextCls != N && nextCls != A && !nextNounish) sc[N] += 1.6
                if (pw == "un" || pw == "una" || pw == "el" || pw == "la") {
                    if (w == "solo" || w == "sola") sc[A] += 3.0
                }
                if (isPart && nextNounish) {
                    sc[A] += 1.8
                    sc[N] -= 0.5
                }
            } else if (afterQuant || numSet.contains(pw)) {
                sc[V] -= 1.0
                sc[N] += 0.4
                if ((knownAdj || isPart) && !ordinalAdj.contains(w) && nextCls != N && nextCls != A &&
                    !(knownVerb && !isPart && !VerbForms.secondSg.contains(w))) sc[N] += 2.3
            }

            // "lo" + adjetivo = sustantivación; "lo" + verbo = pronombre.
            if (pw == "lo" && knownAdj && nextCls != N) sc[N] += 1.8

            // Tras preposición: verbo solo en infinitivo/gerundio.
            if (afterPrep) {
                if (!isInfinitive && !isGerund) {
                    sc[V] -= 2.8
                    sc[N] += 0.6
                    if (isPart) {
                        sc[A] += 0.8
                        if (nextCls != N && nextCls != A) sc[N] += 2.4
                    } else if (knownAdj && nextCls != N && nextCls != A && !soft) {
                        sc[N] += 1.8
                    }
                } else {
                    sc[V] += 1.5
                }
            }

            // Pronombre/negación/relativo delante: verbo conjugado.
            if (preVerbal.contains(pw) && !afterStrongDet) {
                if (knownVerb && !isPart) sc[V] += 2.2 else if (!knownAdj && !isInfinitive) sc[V] += 1.2
                if (pw == "yo" && VerbForms.firstPerson.contains(w)) sc[V] += 2.0
                if (pw == "que" || pw == "quien" || pw == "quienes") sc[V] -= 0.2
            }

            // Adverbio + participio: "recién horneado", "bien hecho".
            if (partAdv.contains(pw) && isPart) {
                sc[A] += 2.0
                sc[V] -= 0.5
            }
            // Dentro de un grupo nominal "det + adjetivo + X": X es el sustantivo.
            if (prev2 != null && strongDet.contains(prev2.lower) && prevCls == A && !isPart &&
                (!knownVerb || verbElsewhere(tokens, cls, i))) {
                sc[N] += 1.8
                sc[V] -= 1.8
            }

            // Cópulas: adjetivo/participio
            val afterSer = serForms.contains(pw)
            val afterEstar = estarForms.contains(pw)
            val afterCop = afterSer || afterEstar || otherCopulas.contains(pw)
            if (afterCop) {
                if (isPart) {
                    if (afterEstar || otherCopulas.contains(pw)) {
                        sc[A] += 1.6
                    } else {
                        sc[V] += 1.3
                    }
                } else if (!isGerund) {
                    sc[A] += 1.8
                    if (!knownVerb || VerbForms.firstPerson.contains(w) || VerbForms.thirdSgA.contains(w)) sc[V] -= 1.0
                }
            }
            // Tiempos compuestos: haber + participio
            if (auxHaber.contains(pw) && (isPart || w.endsWith("to") || w.endsWith("cho"))) {
                sc[V] += 4.0
                sc[A] -= 3.0
                t.aux = false
            }

            // Adverbios de grado delante de un adjetivo
            if (!lexAdverb && (degreeAdv.contains(pw) || (prev.lower.length > 7 && prev.lower.endsWith("mente") && prevCls == R))) {
                val strong = strictDegree.contains(pw) || pw.endsWith("mente")
                if (knownAdj || isPart || adjSuffixWeak(w)) {
                    sc[A] += 2.4
                    sc[V] -= 1.5
                } else if (strong && !knownVerb) {
                    sc[A] += 2.2
                    sc[N] -= 0.8
                }
            }

            // Sustantivo + adjetivo / participio
            if (prevCls == N && !soft && !afterStrongDet) {
                if (isPart) {
                    sc[A] += 1.6
                    sc[V] -= 0.6
                } else if (knownAdj) {
                    sc[A] += 1.3
                } else if (adjSuffixWeak(w)) {
                    sc[A] += 0.9
                } else if (!knownVerb && endsAdjMorph && !afterPrep) {
                    sc[A] += 0.7
                }
            }
            // Acuerdo de género y número entre un sustantivo y una palabra desconocida que lo sigue.
            if (prevCls == N && !soft && !afterStrongDet && !afterPrep && !knownVerb && !knownAdj &&
                !nounSuffix(w) && !isNounExc(w) && !nounFirst.contains(w) && nextCls != N &&
                !(next != null && strongDet.contains(next.lower)) &&
                agrees(pw, w)) {
                sc[A] += 1.1
            }
            // Adjetivo + sustantivo ("importante desafío"): lo que sigue a un adjetivo prenominal suele ser nombre.
            if (prevCls == A && !soft && !knownAdj && !knownVerb && !isPart && !lexAdverb) sc[N] += 0.9

            // Coordinación: "X y Y" → misma clase
            if ((prev2 != null && conj.contains(pw)) && prev2Cls in 0..3) {
                val c2 = prev2Cls
                when (c2) {
                    A -> if (knownAdj || isPart) sc[A] += 1.4 else if (adjSuffixWeak(w)) sc[A] += 0.3
                    V -> if (knownVerb) sc[V] += 1.2
                    N -> if (!knownAdj && !isPart) sc[N] += 0.8
                    R -> if (sc[R] > 1.0) sc[R] += 1.0
                }
            } else if (prev != null && prev2 != null && t.gap.contains(',') && prevCls in 0..3 && !afterPrep) {
                // Enumeraciones: "rápido, eficiente, seguro"
                if (prevCls == A && (knownAdj || adjSuffixWeak(w))) sc[A] += 0.8
                if (prevCls == N && nextCls == N) sc[N] += 0.4
            }

            // Verbos seguidos de un determinante/complemento
            if (!afterStrongDet && !afterPrep && next != null && prevCls != -1) {
                val nw = next.lower
                if (strongDet.contains(nw) && (knownVerb || !knownAdj) && !isPart) {
                    if (prevCls == N || prevCls == A || prevCls == R || prevCls == F || preVerbal.contains(pw)) {
                        sc[V] += if (!knownVerb && !knownAdj && prevCls == N) 2.0 else 1.2
                        if (nounFirst.contains(w) && !next.gap.contains(',')) sc[N] -= 0.8
                    }
                }
            }
        } else {
            // Inicio de oración: verbos en infinitivo o forma conjugada seguida de complemento.
            if (next != null && strongDet.contains(next.lower) && knownVerb && !isPart) {
                sc[V] += if (VerbForms.firstPerson.contains(w) || VerbForms.thirdSgA.contains(w)) 1.5 else 0.5
                if (nounFirst.contains(w)) sc[N] -= 0.8
            }
        }

        // Nombres de idiomas tras un verbo o preposición: sustantivo ("aprendió inglés", "en francés").
        if (langs.contains(w) && prev != null && nextCls != N && nextCls != A &&
            (prevCls == V || prepSet.contains(pw)) && !serForms.contains(pw) && !estarForms.contains(pw)) {
            sc[N] += 2.5
        }

        // Palabras que son a la vez adjetivo y adverbio ("mejor", "claro", "justo", "bajo"…).
        if (bothKnown) {
            val afterDetOrNoun = prev != null && (strongDet.contains(pw) || prevCls == N)
            val afterCop2 = prev != null && (serForms.contains(pw) || estarForms.contains(pw))
            when {
                afterDetOrNoun || afterCop2 -> sc[A] += 1.5
                prevCls == V -> sc[R] += 1.5
                nextCls == N -> sc[A] += 1.5
                nextCls == A || nextCls == V -> sc[R] += 1.5
                else -> sc[R] += 0.3
            }
        }

        // Palabras que son nombre y adverbio ("mañana", "tarde"): adverbio tras verbo, nombre tras determinante.
        if (sc[N] >= 3.0 && sc[R] >= 3.0 && prev != null) {
            if (strongDet.contains(pw) || prepSet.contains(pw)) sc[N] += 1.5
            else if (prevCls == V) sc[R] += 1.5
        }

        // Si lo que sigue es un adjetivo, esta palabra suele ser el sustantivo al que modifica.
        if (nextCls == A && !knownAdj && !isPart && !soft && !(knownVerb && prevCls == N) && prevCls != N) {
            sc[N] += 0.7
            sc[A] -= 0.7
        }

        // Adjetivo antepuesto: "importante desafío", "gran avance".
        if (nextCls == N && knownAdj && !soft && !isNounExc(w)) sc[A] += 0.7

        // Sustantivos léxicos seguidos de "de" + sustantivo: lectura nominal.
        if (next != null && (next.lower == "de" || next.lower == "del") && nounFirst.contains(w)) sc[N] += 0.8

        // Sustantivos deverbales como sujeto ("El uso … permite"): ya cubierto por determinante.

        // Palabras que son nombre y verbo y van tras un verbo conjugado + objeto sin determinante: se mantiene verbo/nombre según prior.
        if (prevCls == V && !soft && nounFirst.contains(w) && prev != null &&
            !serForms.contains(pw) && !auxHaber.contains(pw)) {
            sc[N] += 0.6
        }
    }

    // ------------------------------------------------------------------ API

    private fun argmax(sc: DoubleArray): Int {
        var best = 0
        for (k in 1 until sc.size) if (sc[k] > sc[best] + 1e-9) best = k
        return best
    }

    override fun tag(tokens: List<Token>) {
        val n = tokens.size
        if (n == 0) return
        val base = Array(n) { DoubleArray(5) }
        val fixed = IntArray(n) { -1 }
        for (i in 0 until n) init(tokens[i], base[i], fixed, i)

        val cls = IntArray(n) { i -> if (fixed[i] >= 0) fixed[i] else argmax(base[i].copyOf(4)) }
        // Actualización secuencial (izquierda → derecha) con preferencia por la clase anterior en empates.
        var log = StringBuilder()
        for (round in 0 until 5) {
            log = StringBuilder()
            var changed = false
            for (i in 0 until n) {
                if (fixed[i] >= 0) continue
                val sc = base[i].copyOf()
                applyContext(tokens, i, sc, cls)
                var best = argmax(sc)
                if (sc[cls[i]] >= sc[best] - 1e-9) best = cls[i]
                if (DEBUG) {
                    log.append("  ${tokens[i].text}: base=${base[i].joinToString { "%.1f".format(it) }} final=${sc.joinToString { "%.1f".format(it) }} -> $best\n")
                }
                if (best != cls[i]) {
                    cls[i] = best
                    changed = true
                }
            }
            if (DEBUG) println("round $round: " + cls.joinToString(""))
            if (!changed) break
        }
        if (DEBUG) print(log)

        for (i in 0 until n) {
            val t = tokens[i]
            t.pos = when (cls[i]) {
                N -> Pos.NOUN
                A -> Pos.ADJ
                V -> Pos.VERB
                R -> Pos.ADV
                NUM -> Pos.NUM
                else -> Pos.FUNC
            }
            t.certain = true
            if (t.pos == Pos.VERB) {
                if (t.lemma == t.lower) t.lemma = VerbForms.lemmaOf[t.lower] ?: t.lower
            } else if (t.pos == Pos.ADJ && VerbForms.isParticiple(t.lower)) {
                var b = t.lower
                if (b.endsWith("s")) b = b.dropLast(1)
                if (b.endsWith("a")) b = b.dropLast(1) + "o"
                t.lemma = b
            } else {
                t.lemma = t.lower
            }
        }
        // Auxiliares: "haber" delante de un participio no aporta significado léxico.
        for (i in 0 until n - 1) {
            val t = tokens[i]
            if (t.pos == Pos.VERB && auxHaber.contains(t.lower)) {
                val nx = tokens[i + 1]
                if (!nx.sentenceStart && nx.pos == Pos.VERB && VerbForms.isParticiple(nx.lower)) t.aux = true
            }
        }
    }
}
