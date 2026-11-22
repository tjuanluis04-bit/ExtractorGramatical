package com.extractor.gramatica.nlp

/** Genera las formas conjugadas de los verbos conocidos y las mapea a su infinitivo. */
object VerbForms {

    val lemmaOf: HashMap<String, String> = HashMap(20000)

    init {
        build()
    }

    private fun add(form: String, inf: String) {
        if (!lemmaOf.containsKey(form)) lemmaOf[form] = inf
    }

    private fun build() {
        // Irregulares primero: tienen prioridad sobre las formas regulares generadas.
        for (line in SpanishLexicon.irregulars) {
            val idx = line.indexOf(':')
            if (idx < 0) continue
            val inf = line.substring(0, idx).trim()
            add(inf, inf)
            for (f in line.substring(idx + 1).split(' ')) {
                if (f.isNotBlank()) add(f.trim(), inf)
            }
        }
        for (item in SpanishLexicon.verbList) {
            val parts = item.split(':')
            val alt: String? = if (parts.size > 1) parts[1] else null
            regular(parts[0], alt)
        }
    }

    private fun regular(inf: String, alt: String?) {
        if (inf.length < 4) return
        val tail = inf.takeLast(2).replace('í', 'i')
        if (tail != "ar" && tail != "er" && tail != "ir") return
        val stem = inf.dropLast(2)
        add(inf, inf)

        val pres: List<String>
        val subj: List<String>
        val pret: List<String>
        val impf: List<String>
        val subj2: List<String>
        if (tail == "ar") {
            pres = listOf("o", "as", "a", "amos", "áis", "an")
            subj = listOf("e", "es", "e", "emos", "éis", "en")
            pret = listOf("é", "aste", "ó", "amos", "asteis", "aron")
            impf = listOf("aba", "abas", "aba", "ábamos", "abais", "aban")
            subj2 = listOf("ara", "aras", "áramos", "arais", "aran")
        } else {
            pres = if (tail == "er") {
                listOf("o", "es", "e", "emos", "éis", "en")
            } else {
                listOf("o", "es", "e", "imos", "ís", "en")
            }
            subj = listOf("a", "as", "a", "amos", "áis", "an")
            pret = listOf("í", "iste", "ió", "imos", "isteis", "ieron")
            impf = listOf("ía", "ías", "ía", "íamos", "íais", "ían")
            subj2 = listOf("iera", "ieras", "iéramos", "ierais", "ieran")
        }

        // Presente y subjuntivo (con raíz alternativa en singular y 3.ª plural).
        val stems = if (alt != null) listOf(stem, alt) else listOf(stem)
        for ((si, s) in stems.withIndex()) {
            for ((i, e) in pres.withIndex()) {
                if (si == 0 || (i != 3 && i != 4)) add(s + e, inf)
            }
            for ((i, e) in subj.withIndex()) {
                if (si == 0 || (i != 3 && i != 4)) add(s + e, inf)
            }
        }
        for (e in pret) add(stem + e, inf)
        for (e in impf) add(stem + e, inf)
        for (e in subj2) add(stem + e, inf)

        // Futuro y condicional se forman sobre el infinitivo.
        val base = inf.replace('í', 'i')
        for (e in listOf("é", "ás", "á", "emos", "éis", "án")) add(base + e, inf)
        for (e in listOf("ía", "ías", "ía", "íamos", "íais", "ían")) add(base + e, inf)

        // Gerundio y participio.
        add(stem + (if (tail == "ar") "ando" else "iendo"), inf)
        val part = stem + (if (tail == "ar") "ad" else "id")
        for (e in listOf("o", "a", "os", "as")) add(part + e, inf)

        // Raíces terminadas en vocal (leer, creer, construir...): y en lugar de i.
        if (tail != "ar" && stem.isNotEmpty() && "aeou".indexOf(stem.last()) >= 0) {
            add(stem + "yó", inf)
            add(stem + "yeron", inf)
            add(stem + "yendo", inf)
            add(stem + "yera", inf)
            if (stem.last() == 'u') {
                for (e in listOf("yo", "yes", "ye", "yen")) add(stem + e, inf)
            }
        }

        // Cambios ortográficos.
        if (inf.endsWith("car")) {
            val s2 = stem.dropLast(1) + "qu"
            add(s2 + "é", inf)
            for (e in subj) add(s2 + e, inf)
        }
        if (inf.endsWith("gar")) {
            val s2 = stem + "u"
            add(s2 + "é", inf)
            for (e in subj) add(s2 + e, inf)
        }
        if (inf.endsWith("zar")) {
            val s2 = stem.dropLast(1) + "c"
            add(s2 + "é", inf)
            for (e in subj) add(s2 + e, inf)
        }
        if (inf.endsWith("ger") || inf.endsWith("gir")) {
            val s2 = stem.dropLast(1) + "j"
            add(s2 + "o", inf)
            for (e in subj) add(s2 + e, inf)
        }
    }

    fun isParticiple(w: String): Boolean {
        if (w.length < 6) return false
        return w.endsWith("ado") || w.endsWith("ada") || w.endsWith("ados") || w.endsWith("adas") ||
            w.endsWith("ido") || w.endsWith("ida") || w.endsWith("idos") || w.endsWith("idas")
    }
}
