package com.example.quiz.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.quiz.domain.Attempt
import com.example.quiz.ui.common.displayDate
import com.example.quiz.ui.common.displayName

@Composable
fun HistoryScreen(
    attempts: List<IndexedValue<Attempt>>,
    onAttemptClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .padding(16.dp)
        ) {
            Text("История попыток", style = MaterialTheme.typography.headlineSmall)

            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("К темам")
            }

            if (attempts.isEmpty()) {
                Text(
                    "Попыток пока нет",
                    modifier = Modifier.padding(top = 20.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                ) {
                    items(attempts, key = { it.index }) { entry ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAttemptClick(entry.index) }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    entry.value.topicName,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(entry.value.startedAt.displayDate())
                                Text("Статус: ${entry.value.status.displayName()}")
                                Text("Баллы: ${entry.value.score}")
                            }
                        }
                    }
                }
            }
        }
    }
}
