package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RecentSearchEntity

/**
 * The chip used to print `search.data` straight out of the database — the
 * pipe-packed row it is stored as, "Test|1995-01-12|12:00|Jaipur, Rajasthan,
 * India|26.91|75.78" — in a card with no width limit, so it ran off the screen
 * and the reader saw the storage format. It shows the name and the birth date
 * now, and the card is bounded.
 */
private data class RecentSearchLabel(val title: String, val subtitle: String?)

private fun labelFor(search: RecentSearchEntity): RecentSearchLabel {
    val parts = search.data.split("|")
    return if (search.type == "MATCHING") {
        // boyName | boyDob | girlName | girlDob
        val boy = parts.getOrNull(0)?.takeIf { it.isNotBlank() }
        val girl = parts.getOrNull(2)?.takeIf { it.isNotBlank() }
        RecentSearchLabel(
            title = listOfNotNull(boy, girl).joinToString(" & ").ifBlank { search.data },
            subtitle = null
        )
    } else {
        // name | dob | tob | place | lat | lng
        RecentSearchLabel(
            title = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: search.data,
            subtitle = parts.getOrNull(1)?.takeIf { it.isNotBlank() }
        )
    }
}

@Composable
fun RecentSearchesComponent(
    recentSearches: List<RecentSearchEntity>,
    onSearchSelected: (RecentSearchEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (recentSearches.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = com.example.util.LanguageManager.getString("हाल की खोजें", "Recent Searches"),
            style = MaterialTheme.typography.titleSmall.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recentSearches, key = { it.id }) { search ->
                val label = labelFor(search)
                GlassCard(
                    modifier = Modifier
                        .widthIn(max = 200.dp)
                        .clickable { onSearchSelected(search) }
                ) {
                    Column {
                        Text(
                            text = label.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                        label.subtitle?.let {
                            Text(
                                text = it,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
