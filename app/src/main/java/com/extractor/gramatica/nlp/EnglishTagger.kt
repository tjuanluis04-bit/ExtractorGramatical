package com.extractor.gramatica.nlp

/** Etiquetador básico para inglés (reglas de sufijos + contexto). */
object EnglishTagger : Tagger {

    private fun set(s: String): Set<String> = s.split(' ').filter { it.isNotBlank() }.toHashSet()

    val stopwords: Set<String> = set(
        "the a an this that these those my your his her its our their some any each every all both " +
            "and or but nor so yet if because although while as than then that which who whom whose " +
            "what when where why how of in on at by for with about against between into through " +
            "during before after above below to from up down out off over under again further once " +
            "i me you he him she it we us they them myself yourself himself herself itself ourselves " +
            "themselves not no only own same such there here other another one two three also"
    )

    private val determiners: Set<String> = set(
        "the a an this that these those my your his her its our their some any each every"
    )

    private val modals: Set<String> = set(
        "can could will would should may might must shall do does did to"
    )

    private val subjects: Set<String> = set("i you we they he she it who")

    private val copulas: Set<String> = set("is are was were be been being am seem seems become becomes")

    private val adverbs: Set<String> = set(
        "very quite always never often still already soon well too now just really almost rather " +
            "perhaps maybe however therefore thus together away today tomorrow yesterday later early " +
            "far near ever even much more most less least"
    )

    private val degree: Set<String> = set("very more most less least too so quite rather extremely")

    private val verbs: Set<String> = set(
        "is are was were be been being am has have had having do does did done can could will " +
            "would should may might must get gets got getting make makes made making go goes went " +
            "gone going see sees saw seen take takes took taken come comes came say says said know " +
            "knows knew known think thinks thought give gives gave given find finds found use uses " +
            "used tell tells told work works worked call calls called try tries tried need needs " +
            "needed feel feels felt become became leave leaves left put puts keep keeps kept let " +
            "begin begins began show shows showed shown hear hears heard play plays played run runs " +
            "ran move moves moved live lives lived believe bring brings brought write writes wrote " +
            "written sit stand lose pay meet include continue set learn change lead understand " +
            "watch follow stop create speak read allow add spend grow open walk win offer remember " +
            "love consider appear buy wait serve die send expect build stay fall cut reach kill " +
            "remain suggest raise pass sell require report decide pull"
    )

    private val adjectives: Set<String> = set(
        "good bad new old big small large little long short high low great important different " +
            "possible simple complex clear free full real young early late easy hard strong weak " +
            "fast slow hot cold open closed public private local national social political " +
            "economic technical scientific global human natural digital main major minor common " +
            "special general specific certain likely whole best better worse worst first last next " +
            "final several many few various similar available necessary useful useless"
    )

    private fun endsWithAny(w: String, vararg s: String): Boolean {
        for (x in s) if (w.endsWith(x)) return true
        return false
    }

    private fun pass1(t: Token) {
        val w = t.lower
        var digits = 0
        for (c in w) if (c.isDigit()) digits++
        if (digits > 0 && digits * 2 >= w.length) {
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
        if (stopwords.contains(w)) {
            t.pos = Pos.FUNC
            t.certain = true
            return
        }
        if (adverbs.contains(w) || (w.length > 4 && w.endsWith("ly"))) {
            t.pos = Pos.ADV
            t.certain = true
            return
        }
        if (verbs.contains(w)) {
            t.pos = Pos.VERB
            t.certain = true
            return
        }
        if (adjectives.contains(w)) {
            t.pos = Pos.ADJ
            t.certain = true
            return
        }
        if (endsWithAny(w, "tion", "sion", "ment", "ness", "ity", "ship", "ism", "ance", "ence", "ology", "graphy")) {
            t.pos = Pos.NOUN
            t.certain = true
            return
        }
        if (endsWithAny(w, "ous", "ful", "able", "ible", "less", "ish", "ive", "ical")) {
            t.pos = Pos.ADJ
            t.certain = true
            return
        }
        t.pos = Pos.NOUN
        t.certain = false
    }

    private fun pass2(tokens: List<Token>, i: Int) {
        val t = tokens[i]
        if (t.pos == Pos.FUNC || t.pos == Pos.NUM || t.pos == Pos.OTHER) return
        val w = t.lower
        val prev: Token? = if (i > 0 && !t.sentenceStart) tokens[i - 1] else null
        val prevDet = prev != null && determiners.contains(prev.lower)

        if (t.pos == Pos.NOUN && !t.proper && t.allowProper && t.text.length > 1 &&
            t.text[0].isUpperCase() && !t.sentenceStart) {
            t.proper = true
            t.certain = true
            return
        }

        // Tras un determinante, una forma verbal pasa a ser sustantivo ("the work", "the run").
        if (t.pos == Pos.VERB && prevDet && !copulas.contains(w)) {
            t.pos = Pos.NOUN
            return
        }
        if (t.certain) return

        if (prevDet) {
            if (w.endsWith("ing") || w.endsWith("ed")) {
                if (w.endsWith("ed")) t.pos = Pos.ADJ
            }
            return
        }
        if (prev != null) {
            val p = prev.lower
            if (modals.contains(p) || subjects.contains(p)) {
                if (!p.equals("it") || w.endsWith("s")) {
                    t.pos = Pos.VERB
                    return
                }
            }
            if (degree.contains(p)) {
                t.pos = Pos.ADJ
                return
            }
            if (copulas.contains(p) && w.length > 4 && endsWithAny(w, "ed", "ing", "al", "ic", "ant", "ent")) {
                t.pos = Pos.ADJ
                return
            }
            if (prev.pos == Pos.NOUN && endsWithAny(w, "ed", "al", "ic", "ant", "ent") && w.length > 4) {
                t.pos = Pos.ADJ
                return
            }
        }
        if (w.length > 5 && w.endsWith("ing")) {
            t.pos = Pos.VERB
            return
        }
        if (w.length > 4 && w.endsWith("ed")) {
            t.pos = Pos.VERB
        }
    }

    override fun tag(tokens: List<Token>) {
        for (t in tokens) pass1(t)
        for (i in tokens.indices) pass2(tokens, i)
    }
}
