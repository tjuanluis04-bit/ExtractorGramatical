package com.extractor.gramatica.nlp

/** Genera las formas conjugadas de los verbos conocidos y las mapea a su infinitivo. */
object VerbForms {

    val lemmaOf: HashMap<String, String> = HashMap(60000)

    /** Formas que son primera persona del presente (coinciden con sustantivos: "trabajo", "cuento"). */
    val firstPerson: HashSet<String> = HashSet(4000)

    /** Participios (también irregulares). */
    val participles: HashSet<String> = HashSet(8000)

    /** Formas de tercera persona singular del presente en -a (coinciden con sustantivos: "cuenta", "forma"). */
    val thirdSgA: HashSet<String> = HashSet(4000)

    /** Formas de segunda persona singular (coinciden con plurales: "casas", "notas", "partes"). */
    val secondSg: HashSet<String> = HashSet(4000)

    /** Infinitivos conocidos. */
    val infinitives: HashSet<String> = HashSet(4000)

    // Verbos compuestos que siguen la conjugación de uno irregular: "mantener>tener".
    private val compounds = listOf(
        "mantener>tener", "obtener>tener", "contener>tener", "detener>tener", "sostener>tener",
        "retener>tener", "entretener>tener", "abstener>tener", "atener>tener",
        "proponer>poner", "suponer>poner", "componer>poner", "exponer>poner", "imponer>poner",
        "oponer>poner", "disponer>poner", "reponer>poner", "anteponer>poner", "posponer>poner",
        "descomponer>poner", "superponer>poner", "sobreponer>poner", "recomponer>poner",
        "convenir>venir", "intervenir>venir", "prevenir>venir", "provenir>venir", "sobrevenir>venir",
        "atraer>traer", "distraer>traer", "extraer>traer", "contraer>traer", "sustraer>traer",
        "abstraer>traer", "retraer>traer", "substraer>traer",
        "deshacer>hacer", "rehacer>hacer", "contrahacer>hacer",
        "predecir>decir", "contradecir>decir", "bendecir>decir", "maldecir>decir",
        "prever>ver", "entrever>ver", "rever>ver",
        "devolver>volver", "envolver>volver", "revolver>volver", "desenvolver>volver",
        "inducir>producir", "deducir>producir", "introducir>producir", "conducir>producir",
        "reproducir>producir", "seducir>producir", "traducir>producir",
        "desconocer>conocer", "reconocer>conocer",
        "desaparecer>parecer", "aparecer>parecer", "reaparecer>parecer",
        "desobedecer>conocer", "agradecer>conocer", "establecer>conocer", "ofrecer>conocer",
        "merecer>conocer", "pertenecer>conocer", "permanecer>conocer", "enriquecer>conocer",
        "fortalecer>conocer", "favorecer>conocer", "obedecer>conocer", "padecer>conocer",
        "prevalecer>conocer", "restablecer>conocer", "amanecer>conocer", "anochecer>conocer",
        "crecer>conocer", "nacer>conocer", "renacer>conocer", "envejecer>conocer", "fallecer>conocer",
        "convencer>vencer", "ejercer>vencer", "torcer>vencer", "esparcir>vencer", "zurcir>vencer",
        "complacer>conocer", "yacer>conocer",
        "prescribir>escribir", "describir>escribir", "inscribir>escribir", "suscribir>escribir",
        "transcribir>escribir", "circunscribir>escribir",
        "cubrir>abrir", "descubrir>abrir", "encubrir>abrir", "recubrir>abrir", "entreabrir>abrir",
        "reabrir>abrir", "disolver>resolver", "absolver>resolver",
        "recaer>caer", "decaer>caer", "sobresalir>salir", "satisfacer>hacer",
        "perseguir>seguir", "conseguir>seguir", "proseguir>seguir", "subseguir>seguir"
    )

    private val extraParticiples = hashSetOf(
        "descrito", "descrita", "descritos", "descritas", "inscrito", "inscrita", "inscritos",
        "transcrito", "transcrita", "prescrito", "suscrito", "descubierto", "descubierta",
        "descubiertos", "descubiertas", "cubierto", "cubierta", "cubiertos", "cubiertas",
        "encubierto", "recubierto", "predicho", "contradicho", "bendito", "maldito", "devuelto",
        "devuelta", "devueltos", "devueltas", "envuelto", "envuelta", "envueltos", "disuelto",
        "disuelta", "revuelto", "satisfecho", "satisfecha", "deshecho", "rehecho", "compuesto",
        "compuesta", "compuestos", "compuestas", "propuesto", "propuesta", "propuestos",
        "propuestas", "supuesto", "supuesta", "supuestos", "expuesto", "expuesta", "expuestos",
        "expuestas", "impuesto", "impuesta", "dispuesto", "dispuesta", "dispuestos", "dispuestas",
        "opuesto", "opuesta", "opuestos", "opuestas", "repuesto", "contenido", "contenida",
        "contenidos", "contenidas", "obtenido", "obtenida", "obtenidos", "obtenidas", "mantenido",
        "mantenida", "mantenidos", "mantenidas", "extraído", "extraída", "extraídos", "extraídas",
        "traído", "traída", "traídos", "traídas", "producido", "producida", "producidos",
        "producidas", "introducido", "introducida", "introducidos", "introducidas", "conducido",
        "conducida", "conducidos", "conducidas", "traducido", "traducida", "traducidos",
        "traducidas", "reproducido", "reproducida", "reproducidos", "reproducidas", "previsto",
        "prevista", "previstos", "previstas", "visto", "vista", "vistos", "vistas", "hecho",
        "hecha", "hechos", "hechas", "dicho", "dicha", "dichos", "dichas", "puesto", "puesta",
        "puestos", "puestas", "escrito", "escrita", "escritos", "escritas", "abierto", "abierta",
        "abiertos", "abiertas", "roto", "rota", "rotos", "rotas", "muerto", "muerta", "muertos",
        "muertas", "resuelto", "resuelta", "resueltos", "resueltas", "vuelto", "vuelta",
        "vueltos", "vueltas", "frito", "impreso", "impresa", "impresos", "impresas", "provisto",
        "provista", "provistos", "provistas", "adscrito", "elegido", "elegida", "elegidos",
        "elegidas", "reelegido", "corregido", "corregida", "corregidos", "corregidas",
        "convertido", "convertida", "convertidos", "convertidas", "sentido", "sentida",
        "sentidos", "sentidas", "conseguido", "conseguida", "conseguidos", "conseguidas",
        "seguido", "seguida", "seguidos", "seguidas", "pedido", "pedida", "pedidos", "pedidas"
    )

    private val irregularInfinitives = HashSet<String>()

    init {
        build()
    }

    private fun add(form: String, inf: String) {
        if (!lemmaOf.containsKey(form)) lemmaOf[form] = inf
    }

    private fun build() {
        val irregularForms = HashMap<String, List<String>>()
        for (line in SpanishLexicon.irregulars) {
            val idx = line.indexOf(':')
            if (idx < 0) continue
            val inf = line.substring(0, idx).trim()
            val forms = line.substring(idx + 1).split(' ').filter { it.isNotBlank() }.map { it.trim() }
            irregularForms[inf] = forms
            irregularInfinitives.add(inf)
            infinitives.add(inf)
            add(inf, inf)
            for (f in forms) add(f, inf)
        }
        // Compuestos de verbos irregulares: prefijo + forma del verbo base.
        for (c in compounds) {
            val parts = c.split('>')
            val inf = parts[0]
            val base = parts[1]
            val forms = irregularForms[base]
            infinitives.add(inf)
            add(inf, inf)
            if (forms != null && inf.endsWith(base)) {
                val prefix = inf.removeSuffix(base)
                for (f in forms) {
                    // Las formas con tilde en la primera sílaba del base pierden la tilde ("dé" no aplica).
                    if (f.length < 3) continue
                    add(prefix + f, inf)
                }
            }
        }
        val all = LinkedHashSet<String>()
        all.addAll(SpanishLexicon.verbList)
        all.addAll(SpanishVocab.verbs)
        for (item in all) {
            val parts = item.split(':')
            if (irregularInfinitives.contains(parts[0])) continue
            val alt: String? = if (parts.size > 1) parts[1] else null
            regular(parts[0], alt)
        }
        for (p in extraParticiples) {
            participles.add(p)
        }
        // Reconocer las formas conocidas de participio en el mapa de lemas.
        for ((f, l) in lemmaOf) {
            if (f.endsWith("ado") || f.endsWith("ido") || f.endsWith("ada") || f.endsWith("ida") ||
                f.endsWith("ados") || f.endsWith("idos") || f.endsWith("adas") || f.endsWith("idas")
            ) {
                participles.add(f)
            }
        }
    }

    private fun lastVowelSwap(stem: String, from: Char, to: Char): String {
        val i = stem.lastIndexOf(from)
        return if (i < 0) stem else stem.substring(0, i) + to + stem.substring(i + 1)
    }

    private fun regular(inf: String, alt: String?) {
        if (inf.length < 4) return
        val tail = inf.takeLast(2).replace('í', 'i')
        if (tail != "ar" && tail != "er" && tail != "ir") return
        val stem = inf.dropLast(2)
        infinitives.add(inf)
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
                if (si == 0 || (i != 3 && i != 4)) {
                    add(s + e, inf)
                    if (i == 0) firstPerson.add(s + e)
                    if (i == 1) secondSg.add(s + e)
                    if (i == 2 && tail == "ar") thirdSgA.add(s + e)
                }
            }
            for ((i, e) in subj.withIndex()) {
                if (si == 0 || (i != 3 && i != 4)) {
                    add(s + e, inf)
                    if (i == 1) secondSg.add(s + e)
                }
            }
        }
        // Verbos con cambio de raíz: con la raíz alterada del presente no coinciden 1.ª/2.ª plural.
        for (e in pret) add(stem + e, inf)
        for (e in impf) add(stem + e, inf)
        for (e in subj2) add(stem + e, inf)

        // En los verbos en -ir con cambio de raíz, la raíz cambia también en 3.ª persona del pretérito y gerundio.
        if (tail == "ir" && alt != null) {
            val s2 = when {
                alt.contains("ie") || (alt.contains("i") && !alt.contains("ue")) -> lastVowelSwap(stem, 'e', 'i')
                alt.contains("ue") -> lastVowelSwap(stem, 'o', 'u')
                else -> stem
            }
            for (e in listOf("ió", "ieron", "iendo", "iera", "ieras", "ieran", "iese", "iesen")) add(s2 + e, inf)
            for (e in listOf("a", "as", "amos", "an")) add(s2 + e, inf)
        }

        // Futuro y condicional se forman sobre el infinitivo.
        val base = inf.replace('í', 'i')
        for (e in listOf("é", "ás", "á", "emos", "éis", "án")) add(base + e, inf)
        for (e in listOf("ía", "ías", "ía", "íamos", "íais", "ían")) add(base + e, inf)

        // Gerundio y participio.
        add(stem + (if (tail == "ar") "ando" else "iendo"), inf)
        val part = stem + (if (tail == "ar") "ad" else "id")
        for (e in listOf("o", "a", "os", "as")) {
            add(part + e, inf)
            participles.add(part + e)
        }

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
        if (inf.endsWith("cer") && stem.length > 2 && "aeiou".indexOf(stem[stem.length - 2]) < 0) {
            // vencer → venzo / venza
            val s2 = stem.dropLast(1) + "z"
            add(s2 + "o", inf)
            for (e in subj) add(s2 + e, inf)
        }
        if (inf.endsWith("guir")) {
            val s2 = stem.dropLast(1)
            add(s2 + "o", inf)
            for (e in subj) add(s2 + e, inf)
        }
        if (inf.endsWith("uar") || inf.endsWith("iar")) {
            // enviar → envío, actuar → actúo (formas acentuadas)
            val last = stem.last()
            val acc = if (last == 'i') 'í' else 'ú'
            val s2 = stem.dropLast(1) + acc
            for ((i, e) in pres.withIndex()) if (i != 3 && i != 4) add(s2 + e, inf)
            for ((i, e) in subj.withIndex()) if (i != 3 && i != 4) add(s2 + e, inf)
        }
        if (inf.endsWith("ucir") || inf.endsWith("ocer") || inf.endsWith("ecer")) {
            // conocer → conozco / conozca
            val s2 = stem.dropLast(1) + "zc"
            add(s2 + "o", inf)
            for (e in subj) add(s2 + e, inf)
        }
        if (inf.endsWith("uir") && !inf.endsWith("guir") && !inf.endsWith("quir")) {
            // construir → construyo, construye, construyen
            val s2 = stem.dropLast(1) + "uy"
            for ((i, e) in pres.withIndex()) if (i != 3 && i != 4) add(s2 + e, inf)
            for (e in subj) add(s2 + e, inf)
        }
    }

    fun isParticiple(w: String): Boolean {
        if (participles.contains(w)) return true
        if (w.length < 6) return false
        return w.endsWith("ado") || w.endsWith("ada") || w.endsWith("ados") || w.endsWith("adas") ||
            w.endsWith("ido") || w.endsWith("ida") || w.endsWith("idos") || w.endsWith("idas")
    }
}
