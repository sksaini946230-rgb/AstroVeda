package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.components.SubTabHeader
import com.example.util.LanguageManager

@Composable
fun MoreScreen(viewModel: MainViewModel) {
    val currentSubTab by viewModel.moreSubTab.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            SubTabHeader(
                selectedTab = currentSubTab,
                tabs = listOf(
                    LanguageManager.getString("सहेजे प्रोफाइल", "Saved Profiles"),
                    LanguageManager.getString("सेटिंग्स", "Settings")
                ),
                onTabSelected = { viewModel.setMoreSubTab(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (currentSubTab == 1) {
                SettingsScreen(
                    viewModel = viewModel,
                    onShowPremiumDialog = { viewModel.showPremiumDialog.value = true }
                )
            } else {
                SavedProfilesScreen(viewModel)
            }
        }
    }
}
