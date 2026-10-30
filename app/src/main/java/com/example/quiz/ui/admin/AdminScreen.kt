
package com.example.quiz.ui.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.quiz.ui.common.displayDate
import com.example.quiz.ui.common.displayName
import java.util.UUID

@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    onBankClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var minutes by rememberSaveable {
        mutableStateOf("")
    }

    var maxQuestions by rememberSaveable {
        mutableStateOf("")
    }

    var attemptToDelete by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var expandedAttempt by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(state.settings) {
        state.settings?.let { settings ->
            minutes = settings.timeLimitMinutes.toString()
            maxQuestions = settings.maxQuestionsPerTopic.toString()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            "application/json"
        )
    ) { uri ->
        if (uri != null) {
            viewModel.exportTo(uri)
        }
    }

    if (attemptToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                attemptToDelete = null
            },
            title = {
                Text("Удалить попытку?")
            },
            text = {
                Text(
                    "Результат будет удалён из истории. " +
                            "Это действие нельзя отменить."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = attemptToDelete
                        attemptToDelete = null

                        if (id != null) {
                            expandedAttempt = null
                            viewModel.deleteAttempt(
                                UUID.fromString(id)
                            )
                        }
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        attemptToDelete = null
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 32.dp
            )
        ) {
            item {
                Text(
                    text = "Администратор",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Выйти")
                }
            }

            item {
                Text(
                    text = "Банк вопросов",
                    style = MaterialTheme.typography.titleLarge
                )

                Button(
                    onClick = onBankClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Просмотреть темы и вопросы")
                }
            }

            item {
                Text(
                    text = "Настройки тестирования",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = minutes,
                    onValueChange = {
                        minutes = it
                    },
                    label = {
                        Text("Время на тест, минут")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    enabled = !state.isBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                OutlinedTextField(
                    value = maxQuestions,
                    onValueChange = {
                        maxQuestions = it
                    },
                    label = {
                        Text("Максимум вопросов на тему")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    enabled = !state.isBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                Button(
                    onClick = {
                        viewModel.saveSettings(
                            minutes,
                            maxQuestions
                        )
                    },
                    enabled = !state.isBusy &&
                            state.settings != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text("Сохранить настройки")
                }
            }

            item {
                Text(
                    text = "Экспорт данных",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "JSON с настройками, темами, " +
                            "вопросами и всеми попытками студентов.",
                    modifier = Modifier.padding(top = 6.dp)
                )

                Button(
                    onClick = {
                        exportLauncher.launch(
                            "quiz_export_${System.currentTimeMillis()}.json"
                        )
                    },
                    enabled = !state.isBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text("Экспортировать JSON")
                }
            }

            if (state.isBusy || state.message != null) {
                item {
                    Text(
                        text = if (state.isBusy) {
                            "Выполняется операция..."
                        } else {
                            state.message.orEmpty()
                        },
                        color = if (state.isError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }

            item {
                Text(
                    text = "Попытки студентов",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Всего попыток: ${state.attempts.size}",
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (state.attempts.isEmpty()) {
                item {
                    Text("Попыток пока нет")
                }
            }

            items(
                items = state.attempts,
                key = { attempt ->
                    attempt.id.toString()
                }
            ) { attempt ->

                val attemptId = attempt.id.toString()

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = attempt.studentLogin,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(attempt.topicName)

                        Text(
                            attempt.startedAt.displayDate()
                        )

                        Text(
                            "Статус: ${attempt.status.displayName()}"
                        )

                        Text("Баллы: ${attempt.score}")

                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    expandedAttempt =
                                        if (
                                            expandedAttempt == attemptId
                                        ) {
                                            null
                                        } else {
                                            attemptId
                                        }
                                }
                            ) {
                                Text(
                                    if (
                                        expandedAttempt == attemptId
                                    ) {
                                        "Свернуть"
                                    } else {
                                        "Подробнее"
                                    }
                                )
                            }

                            TextButton(
                                onClick = {
                                    attemptToDelete = attemptId
                                },
                                enabled = !state.isBusy
                            ) {
                                Text("Удалить")
                            }
                        }

                        if (expandedAttempt == attemptId) {
                            Text(
                                "Правильных: ${attempt.correctAnswers}"
                            )

                            Text(
                                "Неправильных: ${attempt.wrongAnswers}"
                            )

                            Text(
                                "По тайм-ауту: ${attempt.timeExpiredAnswers}"
                            )

                            if (attempt.answers.isEmpty()) {
                                Text("Ответов пока нет")
                            }

                            attempt.answers.forEachIndexed {
                                    index, answer ->

                                Text(
                                    text = "Вопрос ${index + 1}: " +
                                            answer.questionText,
                                    style =
                                        MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(top = 8.dp)
                                )

                                Text(
                                    if (answer.timedOut) {
                                        "Время вышло"
                                    } else if (answer.isCorrect) {
                                        "Верно"
                                    } else {
                                        "Неверно"
                                    }
                                )

                                Text(
                                    "Ответ: ${answer.studentAnswer}"
                                )

                                Text(
                                    "Правильный ответ: " +
                                            answer.correctAnswer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
