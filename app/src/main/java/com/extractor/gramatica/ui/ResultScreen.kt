@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.extractor.gramatica.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.extractor.gramatica.nlp.Analysis
import com.extractor.gramatica.nlp.Category
import com.extractor.gramatica.nlp.Entry

private fun copy(context: Context, label: String, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "Copiado", Toast.LENGTH_SHORT).show()
}

@Composable
fun ResultScreen(vm: MainViewModel, analysis: Analysis) {
    val context = LocalContext.current
    val dark = LocalDarkTheme.current

    val visible: List<Entry> = remember(analysis, vm.selected, vm.query, vm.sortMode) {
        var list = analysis.entries[vm.selected].orEmpty()
        val q = vm.query.trim().lowercase()
        if (q.isNotEmpty()) list = list.filter { it.lemma.lowercase().contains(q) }
        if (vm.sortMode == SortMode.ALPHABETIC) list = list.sortedBy { it.lemma.lowercase() }
        list
    }
    val maxCount = visible.maxOfOrNull { it.count } ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Barra superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = vm::backToInput) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver")
            }
            Text(
                text = "Resultados",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { copy(context, "Informe", Export.fullReport(analysis)) }) {
                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copiar todo")
            }
            IconButton(onClick = { Export.share(context, "Extractor Gramatical", Export.fullReport(analysis)) }) {
                Icon(Icons.Outlined.Share, contentDescription = "Compartir")
            }
        }

        // Resumen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(analysis.totalWords.toString(), "palabras", Modifier.weight(1f))
            StatCard(analysis.uniqueWords.toString(), "únicas", Modifier.weight(1f))
            StatCard(analysis.lang.label, "idioma", Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))

        // Pestañas de categoría
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Category.values().toList()) { c ->
                CategoryPill(
                    category = c,
                    count = analysis.countOf(c),
                    selected = vm.selected == c,
                    onClick = { vm.selectCategory(c) }
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        // Vista lista / texto
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            SegmentedButton(
                selected = vm.viewMode == ViewMode.LIST,
                onClick = { vm.updateViewMode(ViewMode.LIST) },
                shape = SegmentedButtonDefaults.itemShape(0, 2)
            ) { Text("Lista") }
            SegmentedButton(
                selected = vm.viewMode == ViewMode.TEXT,
                onClick = { vm.updateViewMode(ViewMode.TEXT) },
                shape = SegmentedButtonDefaults.itemShape(1, 2)
            ) { Text("Texto resaltado") }
        }
        Spacer(Modifier.height(10.dp))

        if (vm.viewMode == ViewMode.LIST) {
            ListPane(vm, visible, maxCount, analysis)
        } else {
            HighlightPane(vm, analysis, dark)
        }
    }
}

@Composable
private fun ListPane(vm: MainViewModel, visible: List<Entry>, maxCount: Int, analysis: Analysis) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = vm.query,
                onValueChange = vm::updateQuery,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Buscar…") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            FilterChip(
                selected = vm.sortMode == SortMode.ALPHABETIC,
                onClick = {
                    vm.updateSort(
                        if (vm.sortMode == SortMode.ALPHABETIC) SortMode.FREQUENCY else SortMode.ALPHABETIC
                    )
                },
                label = { Text(if (vm.sortMode == SortMode.ALPHABETIC) "A–Z" else "Frecuencia") },
                shape = RoundedCornerShape(50)
            )
        }
        Spacer(Modifier.height(8.dp))

        if (visible.isEmpty()) {
            EmptyState(
                title = "Sin resultados",
                subtitle = if (vm.query.isNotBlank()) "Ninguna coincidencia para “${vm.query}”."
                else "No se encontró ${vm.selected.plural.lowercase()} en este texto."
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(visible, key = { it.category.name + it.lemma }) { e ->
                    EntryRow(e, maxCount) { copy(context, e.lemma, e.lemma) }
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                onClick = {
                    copy(context, vm.selected.plural, Export.categoryText(visible, withCounts = false))
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "Copiar ${vm.selected.plural.lowercase()} (${visible.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun HighlightPane(vm: MainViewModel, analysis: Analysis, dark: Boolean) {
    val scroll = rememberScrollState()

    val annotated: AnnotatedString = remember(analysis, vm.highlighted, dark) {
        val text = analysis.text
        val active = analysis.spans.filter { vm.highlighted.contains(it.category) }
        // Prioridad: terminología > resto; las zonas que se solapan se resuelven a favor de la más larga.
        val ordered = active.sortedWith(compareBy({ it.start }, { -(it.end - it.start) }))
        buildAnnotatedString {
            var pos = 0
            for (s in ordered) {
                if (s.start < pos) continue
                if (s.start > pos) append(text.substring(pos, s.start))
                val color = categoryColor(s.category, dark)
                withStyle(
                    SpanStyle(
                        color = color,
                        fontWeight = FontWeight.SemiBold,
                        background = color.copy(alpha = 0.13f)
                    )
                ) { append(text.substring(s.start, s.end)) }
                pos = s.end
            }
            if (pos < text.length) append(text.substring(pos))
        }
    }

    Column(Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Category.values().toList()) { c ->
                val on = vm.highlighted.contains(c)
                val color = categoryColor(c, dark)
                FilterChip(
                    selected = on,
                    onClick = { vm.toggleHighlight(c) },
                    label = { Text(c.singular) },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, if (on) color else MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                .navigationBarsPadding(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Text(
                text = annotated,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .verticalScroll(scroll)
                    .padding(18.dp)
            )
        }
    }
}
