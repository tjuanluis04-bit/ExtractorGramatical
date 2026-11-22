package com.extractor.gramatica.ui

import android.content.Context
import android.content.Intent
import com.extractor.gramatica.nlp.Analysis
import com.extractor.gramatica.nlp.Category
import com.extractor.gramatica.nlp.Entry

object Export {

    fun entryLine(e: Entry): String =
        if (e.category == Category.TERM || e.count <= 1) e.lemma else "${e.lemma} (${e.count})"

    /** Texto de una sola categoría, una palabra por línea. */
    fun categoryText(entries: List<Entry>, withCounts: Boolean): String =
        entries.joinToString("\n") { if (withCounts) entryLine(it) else it.lemma }

    /** Informe completo con todas las categorías. */
    fun fullReport(analysis: Analysis): String {
        val sb = StringBuilder()
        for (cat in Category.values()) {
            val list = analysis.entries[cat].orEmpty()
            if (list.isEmpty()) continue
            sb.append(cat.plural.uppercase()).append(" (").append(list.size).append(")\n")
            sb.append(list.joinToString(", ") { entryLine(it) })
            sb.append("\n\n")
        }
        return sb.toString().trim()
    }

    fun share(context: Context, title: String, text: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(send, title))
    }
}
