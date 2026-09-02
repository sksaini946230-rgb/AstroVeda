package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RecentSearchEntity

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
            items(recentSearches) { search ->
                GlassCard(
                    modifier = Modifier.clickable { onSearchSelected(search) }
                ) {
                    Text(
                        text = search.data, // Should probably be parsed, but this is a quick start
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
