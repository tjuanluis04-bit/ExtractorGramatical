package com.extractor.gramatica.nlp

/**
 * Etiquetador para inglés: léxico + morfología verbal generada + desambiguación por contexto.
 * Cada palabra obtiene un conjunto de clases posibles (N, A, V, R) y el contexto elige una.
 */
object EnglishTagger : Tagger {

    private fun set(s: String): Set<String> = s.split(' ').filter { it.isNotBlank() }.toHashSet()

    val stopwords: Set<String> = EnglishVocab.stopwords

    private val beForms = set("is am are was were be been being")
    private val modals = set("can could will would should may might must shall do does did cannot")
    private val haveForms = set("has have had having")
    private val dets = set(
        "the a an this that these those my your his her its our their some any each every many few " +
            "several both all no another other such"
    )
    private val subjects = set("i you we they he she it who")
    private val preps = set(
        "in on at by for with about against between into through during before after above below " +
            "from over under of within without upon across along around among toward towards via per"
    )
    private val relatives = set("which who whom whose")
    private val conj = set("and or but nor so yet if because although though while as than then when where whether unless until since whereas")
    private val degree = set("very more most less least too so quite rather extremely really")

    private val nounFirst = set(
        "music topic topics logic panic physics politics economics statistics mathematics electronics " +
            "tourist tourists mechanic mechanics critic critics scientist scientists specialist specialists " +
            "student students patient patients"
    )

    private const val N = 1
    private const val A = 2
    private const val V = 4
    private const val R = 8
    private const val UNK = 16

    // ---- morfología verbal generada ----
    private val baseV = HashSet<String>()
    private val sForm = HashMap<String, String>()
    private val pastForm = HashMap<String, String>()
    private val ingForm = HashMap<String, String>()

    private fun isVowel(c: Char) = c in "aeiou"

    init {
        for (b in EnglishVocab.verbs) {
            baseV.add(b)
            val s = when {
                b.endsWith("s") || b.endsWith("x") || b.endsWith("z") || b.endsWith("ch") || b.endsWith("sh") -> b + "es"
                b.endsWith("y") && b.length > 1 && !isVowel(b[b.length - 2]) -> b.dropLast(1) + "ies"
                else -> b + "s"
            }
            sForm[s] = b
            val past = when {
                b.endsWith("e") -> b + "d"
                b.endsWith("y") && b.length > 1 && !isVowel(b[b.length - 2]) -> b.dropLast(1) + "ied"
                else -> b + "ed"
            }
            pastForm[past] = b
            val ing = when {
                b.endsWith("ie") -> b.dropLast(2) + "ying"
                b.endsWith("e") && !b.endsWith("ee") && !b.endsWith("ye") && !b.endsWith("oe") -> b.dropLast(1) + "ing"
                else -> b + "ing"
            }
            ingForm[ing] = b
            val n = b.length
            if (n >= 3 && !isVowel(b[n - 1]) && b[n - 1] !in "wxy" && isVowel(b[n - 2]) && !isVowel(b[n - 3])) {
                val d = b + b[n - 1]
                pastForm[d + "ed"] = b
                ingForm[d + "ing"] = b
            }
        }
        for (line in EnglishVocab.irregular) {
            val c = line.indexOf(':')
            val base = line.substring(0, c)
            val parts = line.substring(c + 1).trim().split(' ').filter { it.isNotBlank() }
            baseV.add(base)
            if (parts.size < 2) continue
            sForm[parts.first()] = base
            if (parts.last().endsWith("ing")) ingForm[parts.last()] = base
            for (i in 1 until parts.size - 1) pastForm[parts[i]] = base
        }
        // formas con ambigüedad de base y pasado: la base ya es verbo
    }

    private fun verbLemma(w: String): String {
        if (beForms.contains(w)) return "be"
        sForm[w]?.let { return it }
        pastForm[w]?.let { return it }
        ingForm[w]?.let { return it }
        if (baseV.contains(w)) return w
        if (w.endsWith("ied") && w.length > 4) return w.dropLast(3) + "y"
        if (w.endsWith("ies") && w.length > 4) return w.dropLast(3) + "y"
        for (suf in listOf("ized", "ised", "ated", "ured", "ified")) {
            if (w.endsWith(suf)) return w.dropLast(1)
        }
        for (suf in listOf("izes", "ises", "ates", "ures")) {
            if (w.endsWith(suf)) return w.dropLast(1)
        }
        for (suf in listOf("izing", "ising", "ating", "uring")) {
            if (w.endsWith(suf)) return w.dropLast(3) + "e"
        }
        if (w.endsWith("ed") && w.length > 4) {
            val st = w.dropLast(2)
            return if (st.length > 2 && st[st.length - 1] == st[st.length - 2] && st.last() !in "lsf") st.dropLast(1) else st
        }
        if (w.endsWith("ing") && w.length > 5) {
            val st = w.dropLast(3)
            return if (st.length > 2 && st[st.length - 1] == st[st.length - 2] && st.last() !in "lsf") st.dropLast(1) else st
        }
        if (w.endsWith("es") && w.length > 4 && (w.endsWith("ches") || w.endsWith("shes") || w.endsWith("sses") || w.endsWith("xes"))) return w.dropLast(2)
        if (w.endsWith("s") && !w.endsWith("ss") && w.length > 3) return w.dropLast(1)
        return w
    }

    private fun endsWithAny(w: String, vararg s: String): Boolean {
        for (x in s) if (w.endsWith(x)) return true
        return false
    }

    /** Clases posibles de una palabra según léxico y morfología (sin contexto). */
    private fun cands(w: String): Int {
        var m = 0
        if (nounFirst.contains(w)) return N
        if (baseV.contains(w)) {
            m = m or V
            if (EnglishVocab.nounVerb.contains(w)) m = m or N
        }
        if (sForm.containsKey(w)) m = m or V or N
        if (pastForm.containsKey(w)) {
            m = m or V
            if (w.endsWith("ed") || w.endsWith("en") || EnglishVocab.adjectives.contains(w)) m = m or A
        }
        if (ingForm.containsKey(w)) m = m or V or N
        if (EnglishVocab.adjectives.contains(w)) m = m or A
        if (EnglishVocab.adverbs.contains(w)) m = m or R
        if (m == 0 && w.length > 4) {
            val st = when {
                w.endsWith("est") -> w.dropLast(3)
                w.endsWith("er") -> w.dropLast(2)
                else -> ""
            }
            if (st.isNotEmpty()) {
                val cs = listOf(st, st + "e", if (st.length > 2 && st.last() == st[st.length - 2]) st.dropLast(1) else st, if (st.endsWith("i")) st.dropLast(1) + "y" else st)
                if (cs.any { EnglishVocab.adjectives.contains(it) }) return A
            }
        }
        if (m != 0) return m
        if (nounFirst.contains(w) || (w.endsWith("s") && nounFirst.contains(w.dropLast(1)))) return N

        // palabra desconocida: sufijos
        if (w.length > 4 && w.endsWith("ly")) {
            if (EnglishVocab.notAdverbLy.contains(w)) return if (w.length > 5 && !w.endsWith("ply")) A else N
            return R
        }
        val base = if (w.endsWith("s") && w.length > 4) w.dropLast(1) else w
        if (endsWithAny(base, "tion", "sion", "ment", "ness", "ity", "ship", "ism", "ance", "ence", "ology", "graphy", "ist", "ogy"))
            return N
        if (endsWithAny(w, "ous", "ful", "able", "ible", "less", "ish", "ive", "ical", "ic", "ary", "ular", "ial") && w.length > 4) return A
        if (w.length > 4 && (w.endsWith("al") || w.endsWith("ant"))) return A or N
        if (endsWithAny(w, "ize", "ise", "ify") && w.length > 5) return V
        if (endsWithAny(w, "izes", "ises", "ifies") && w.length > 6) return V or N
        if (endsWithAny(w, "ized", "ised", "ified") && w.length > 6) return V or A
        if (endsWithAny(w, "izing", "ising", "ifying") && w.length > 7) return V or N
        if (w.endsWith("ate") && w.length > 6) return V or N
        if (w.endsWith("ing") && w.length > 5) return V or N
        if (w.endsWith("ed") && w.length > 4) return V or A
        return N or UNK
    }

    override fun tag(tokens: List<Token>) {
        val n = tokens.size
        val fixed = BooleanArray(n)
        val mask = IntArray(n)

        // 1) clases fijas
        for (i in 0 until n) {
            val t = tokens[i]
            val w = t.lower
            var digits = 0
            for (c in w) if (c.isDigit()) digits++
            when {
                digits > 0 && digits * 2 >= w.length -> { t.pos = Pos.NUM; t.certain = true; fixed[i] = true }
                t.acronym -> { t.pos = Pos.NOUN; t.proper = true; t.certain = true; fixed[i] = true }
                beForms.contains(w) -> {
                    t.pos = Pos.VERB; t.lemma = "be"; t.certain = true; fixed[i] = true
                }
                stopwords.contains(w) -> { t.pos = Pos.FUNC; t.certain = true; fixed[i] = true }
                !t.sentenceStart && t.allowProper && t.text.length > 1 && t.text[0].isUpperCase() &&
                    !EnglishVocab.adjectives.contains(w) -> {
                    t.pos = Pos.NOUN; t.proper = true; t.certain = true; fixed[i] = true
                }
                else -> mask[i] = cands(w)
            }
        }

        // 2) elección por contexto, de izquierda a derecha
        var verbSeen = false
        for (i in 0 until n) {
            val t = tokens[i]
            val w = t.lower
            if (t.sentenceStart) verbSeen = false
            if (fixed[i]) {
                if (t.pos == Pos.VERB || modals.contains(w) || haveForms.contains(w)) verbSeen = true
                if (conj.contains(w) || relatives.contains(w) || w == "that") verbSeen = false
                continue
            }
            val m = mask[i]

            // contexto previo (se saltan adverbios y "not")
            var p = i - 1
            while (p >= 0 && !tokens[p + 1].sentenceStart &&
                (tokens[p].pos == Pos.ADV || tokens[p].lower == "not")) p--
            val prev: Token? = if (p >= 0 && !t.sentenceStart && !tokens[p + 1].sentenceStart) tokens[p] else null
            val pw = prev?.lower ?: ""
            val immediate = prev != null && p == i - 1
            val next: Token? = if (i + 1 < n && !tokens[i + 1].sentenceStart) tokens[i + 1] else null
            val nm = if (next != null && !fixedFunc(next, fixed, tokens, i + 1)) maskOf(next, fixed, mask, i + 1) else 0
            val nextNounish = next != null && (nm and N) != 0 &&
                !(nm and V != 0 && (nm and N) == 0)
            val nextNoun = nextNounish
            val nextVerbish = next != null && (next.pos == Pos.VERB && fixed[i + 1] || (nm and V) != 0 && (nm and N) == 0)

            val pDet = prev != null && dets.contains(pw)
            val pAdjective = prev != null && immediate && prev.pos == Pos.ADJ
            val pNoun = prev != null && immediate && prev.pos == Pos.NOUN
            val pSubj = prev != null && (subjects.contains(pw) || modals.contains(pw) || pw == "to")
            val pBe = prev != null && beForms.contains(pw)
            val pHave = prev != null && haveForms.contains(pw)
            val pPrep = prev != null && immediate && preps.contains(pw)
            val pRel = prev != null && immediate && (relatives.contains(pw) ||
                (pw == "that" && ((sForm.containsKey(w) || pastForm.containsKey(w)) || (baseV.contains(w) && !EnglishVocab.nounVerb.contains(w)))))
            val pDegree = prev != null && degree.contains(pw)
            val pConj = prev != null && immediate && (pw == "and" || pw == "or" || pw == "but")

            var pos: Pos? = null
            val onlyOne = Integer.bitCount(m and 15) == 1 && (m and UNK) == 0

            if (onlyOne) {
                pos = when (m) { N -> Pos.NOUN; A -> Pos.ADJ; V -> Pos.VERB; else -> Pos.ADV }
                // un verbo "seguro" tras determinante sigue siendo nombre
                if (pos == Pos.VERB && pDet && immediate)
                    pos = Pos.NOUN
            } else if (pDet && immediate && !pRel) {
                pos = when {
                    (m and A) != 0 && nextNoun -> Pos.ADJ
                    (m and N) != 0 -> Pos.NOUN
                    (m and A) != 0 -> Pos.ADJ
                    else -> Pos.NOUN
                }
            } else if (pRel && (m and V) != 0) {
                pos = Pos.VERB
            } else if (pSubj && ((m and V) != 0 || ((m and UNK) != 0 && (pw == "to" || modals.contains(pw) || subjects.contains(pw)))) && !(pw == "to" && next != null && dets.contains(next.lower) && false)) {
                pos = Pos.VERB
            } else if (pBe) {
                pos = when {
                    (m and A) != 0 && (EnglishVocab.adjectives.contains(w) || (m and V) == 0) -> Pos.ADJ
                    (m and V) != 0 -> Pos.VERB
                    (m and A) != 0 -> Pos.ADJ
                    else -> Pos.NOUN
                }
            } else if (pHave && (m and V) != 0) {
                pos = Pos.VERB
            } else if (ingForm.containsKey(w) && (m and V) != 0 && prev != null && immediate && (conj.contains(pw) || pw == "to") && !sForm.containsKey(w)) {
                pos = Pos.VERB
            } else if (pDegree && (m and A) != 0) {
                pos = Pos.ADJ
            } else if (pPrep) {
                pos = when {
                    (m and V) != 0 && ingForm.containsKey(w) && next != null && !nextNounish -> Pos.VERB
                    (m and V) != 0 && ingForm.containsKey(w) && next != null && (next.lower in preps || next.lower == "to" || dets.contains(next.lower)) -> Pos.VERB
                    (m and A) != 0 && nextNoun -> Pos.ADJ
                    (m and N) != 0 -> Pos.NOUN
                    (m and A) != 0 -> Pos.ADJ
                    else -> Pos.NOUN
                }
            } else if (prev != null && prev.pos == Pos.NOUN) {
                // sustantivo + palabra ambigua
                pos = when {
                    (m and V) != 0 && baseV.contains(w) && !verbSeen && prev != null && pw.endsWith("s") && !pw.endsWith("ss") -> Pos.VERB
                    (m and R) != 0 && !nextNoun && (m and N) == 0 -> Pos.ADV
                    (m and V) != 0 && (sForm.containsKey(w) || pastForm.containsKey(w)) && !verbSeen -> Pos.VERB
                    (m and V) != 0 && baseV.contains(w) && !verbSeen && !EnglishVocab.nounVerb.contains(w) && (m and N) == 0 -> Pos.VERB
                    (m and A) != 0 && nextNoun -> Pos.ADJ
                    (m and N) != 0 -> Pos.NOUN
                    (m and V) != 0 && pastForm.containsKey(w) && (m and A) != 0 -> Pos.ADJ
                    (m and A) != 0 -> Pos.ADJ
                    (m and V) != 0 -> Pos.VERB
                    else -> Pos.NOUN
                }
                // "data privacy remains": nombre compuesto cuando lo que sigue es un verbo
                if (pos == Pos.VERB && !verbSeen && nextVerbish && (m and N) != 0) pos = Pos.NOUN
            } else if (pAdjective) {
                pos = when {
                    (m and N) != 0 && !(ingForm.containsKey(w) && nextNounish) -> Pos.NOUN
                    (m and A) != 0 && nextNoun -> Pos.ADJ
                    (m and V) != 0 && !verbSeen && pastForm.containsKey(w) -> Pos.VERB
                    else -> Pos.NOUN
                }
            } else if (pConj && prev != null && p >= 1 && !tokens[p].sentenceStart) {
                val pp = tokens[p - 1].pos
                pos = when {
                    pp == Pos.NOUN && (m and V) != 0 && (sForm.containsKey(w) || pastForm.containsKey(w)) && verbSeen && next != null && (nextNounish || (nm and A) != 0 || dets.contains(next.lower)) -> Pos.VERB
                    pp == Pos.ADJ && (m and A) != 0 -> Pos.ADJ
                    pp == Pos.VERB && (m and V) != 0 -> Pos.VERB
                    pp == Pos.NOUN && (m and N) != 0 -> Pos.NOUN
                    pp == Pos.ADV && (m and R) != 0 -> Pos.ADV
                    else -> null
                }
            }

            if (pos == null) {
                // sin contexto claro
                pos = when {
                    (m and R) != 0 && (m and A) != 0 -> if (nextNoun) Pos.ADJ else Pos.ADV
                    (m and R) != 0 && (m and (N or V)) == 0 -> Pos.ADV
                    (m and R) != 0 -> if (nextNoun) Pos.ADJ else Pos.ADV
                    t.sentenceStart && ingForm.containsKey(w) && (m and N) != 0 && !verbSeen && nextVerbish -> Pos.NOUN
                    t.sentenceStart && (m and N) != 0 && (m and V) != 0 && nextVerbish -> Pos.NOUN
                    (m and A) != 0 && nextNoun -> Pos.ADJ
                    (m and V) != 0 && !verbSeen && (m and N) == 0 -> Pos.VERB
                    (m and V) != 0 && !verbSeen && (sForm.containsKey(w) || pastForm.containsKey(w)) && !nextNoun && prev != null -> Pos.VERB
                    (m and N) != 0 -> Pos.NOUN
                    (m and A) != 0 -> Pos.ADJ
                    (m and V) != 0 -> Pos.VERB
                    else -> Pos.NOUN
                }
            }

            t.pos = pos
            t.certain = onlyOne
            if (pos == Pos.VERB) {
                t.lemma = verbLemma(w)
                verbSeen = true
            }
            // ajuste: el adjetivo participial conserva su forma
            if (pos == Pos.ADJ || pos == Pos.NOUN || pos == Pos.ADV) t.lemma = w
        }
    }

    private fun fixedFunc(t: Token, fixed: BooleanArray, tokens: List<Token>, idx: Int): Boolean =
        fixed[idx] && t.pos == Pos.FUNC

    private fun maskOf(t: Token, fixed: BooleanArray, mask: IntArray, idx: Int): Int {
        if (fixed[idx]) {
            return when (t.pos) {
                Pos.NOUN -> N
                Pos.VERB -> V
                else -> 0
            }
        }
        return mask[idx]
    }

    private fun nextIsPlainNoun(t: Token, nm: Int): Boolean = (nm and N) != 0
}
