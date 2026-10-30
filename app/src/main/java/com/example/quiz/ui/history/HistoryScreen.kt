
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
import java.util.UUID

@Composable
fun HistoryScreen(
    attempts: List<Attempt>,
    onAttemptClick: (UUID) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "История попыток",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("К темам")
            }

            if (attempts.isEmpty()) {
                Text(
                    text = "Попыток пока нет",
                    modifier = Modifier.padding(top = 20.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement =
                        Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(
                        top = 12.dp,
                        bottom = 24.dp
                    )
                ) {
                    items(
                        items = attempts,
                        key = { it.id.toString() }
                    ) { attempt ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAttemptClick(attempt.id)
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement =
                                    Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = attempt.topicName,
                                    style =
                                        MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = attempt.startedAt.displayDate()
                                )

                                Text(
                                    text = "Статус: " +
                                            attempt.status.displayName()
                                )

                                Text(
                                    text = "Баллы: ${attempt.score}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
