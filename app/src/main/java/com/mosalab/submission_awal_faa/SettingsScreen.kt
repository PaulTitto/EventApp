package com.mosalab.submission_awal_faa.Screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mosalab.submission_awal_faa.MainViewModel
import com.mosalab.submission_awal_faa.PreferencesManager
import kotlinx.coroutines.launch

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, navController: NavController) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val scope = rememberCoroutineScope()

    val isDarkMode by preferencesManager.isDarkMode.collectAsState(initial = false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = if (isDarkMode) "Dark Mode is On" else "Light Mode is On")
        Spacer(modifier = Modifier.height(16.dp))
        Switch(
            checked = isDarkMode,
            onCheckedChange = { enabled ->
                scope.launch {
                    preferencesManager.setDarkMode(enabled)
                }
            }
        )
    }
}
