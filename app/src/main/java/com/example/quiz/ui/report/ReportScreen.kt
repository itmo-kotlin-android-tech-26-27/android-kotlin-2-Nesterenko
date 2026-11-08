package com.example.quiz.ui.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
fun ReportScreen(
    attempt: Attempt?,
    onRetryClick: (Long) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Отчёт о попытке",
                style = MaterialTheme.typography.headlineSmall
            )

            if (attempt == null) {
                Text("Попытка не найдена")
                OutlinedButton(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Назад")
                }
                return@Column
            }

            Text(attempt.topicName, style = MaterialTheme.typography.titleLarge)
            Text("Студент: ${attempt.studentLogin}")
            Text("Начало: ${attempt.startedAt.displayDate()}")
            Text("Статус: ${attempt.status.displayName()}")

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Баллы: ${attempt.score}", style = MaterialTheme.typography.titleLarge)
                    Text("Правильных ответов: ${attempt.correctAnswers}")
                    Text("Неправильных ответов: ${attempt.wrongAnswers}")
                    Text("Ответов с истекшим временем: ${attempt.timeExpiredAnswers}")
                }
            }

            val topicResults = attempt.getTopicResults()
            if (topicResults.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text("Результаты по темам", style = MaterialTheme.typography.titleMedium)

                topicResults.forEach { result ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(result.topicName, style = MaterialTheme.typography.titleSmall)
                            Text("Баллы: ${result.score}")
                            Text("Правильно: ${result.correctAnswers}; неправильно: ${result.wrongAnswers}")
                            Text("По тайм-ауту: ${result.timeExpiredAnswers}")
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Text("Ответы на вопросы", style = MaterialTheme.typography.titleMedium)

            if (attempt.answers.isEmpty()) {
                Text("Ответов пока нет")
            } else {
                attempt.answers.forEachIndexed { index, answer ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Вопрос ${index + 1}. ${answer.questionText}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                when {
                                    answer.timedOut -> "Время на ответ истекло"
                                    answer.isCorrect -> "Верно"
                                    else -> "Неверно"
                                }
                            )
                            Text("Ваш ответ: ${answer.studentAnswer.ifBlank { "—" }}")
                            Text("Правильный ответ: ${answer.correctAnswer}")
                        }
                    }
                }
            }

            attempt.topicId?.let { topicId ->
                Button(
                    onClick = { onRetryClick(topicId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Пройти ещё раз")
                }
            }

            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Назад")
            }
        }
    }
}
