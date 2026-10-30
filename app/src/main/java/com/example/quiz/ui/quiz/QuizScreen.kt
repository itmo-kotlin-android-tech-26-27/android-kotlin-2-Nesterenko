
package com.example.quiz.ui.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.quiz.domain.MatchParQuestion
import com.example.quiz.domain.MultipleListQ
import com.example.quiz.domain.OpenQ
import kotlinx.coroutines.delay

@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onQuizFinished: () -> Unit,
    onQuizInterrupted: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        viewModel.interruptQuiz()
        onQuizInterrupted()
    }

    LaunchedEffect(state.isFinished) {
        while (!state.isFinished) {
            delay(1000)
            viewModel.refreshTime()
        }
    }

    LaunchedEffect(
        state.isFinished,
        state.isInterrupted
    ) {
        if (state.isFinished && !state.isInterrupted) {
            onQuizFinished()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = state.topicName.ifBlank { "Тест" },
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Уровень: ${state.currentLevel} / ${state.maxLevel}"
            )

            Text(
                text = "Осталось времени: ${formatTime(state.remainingSeconds)}"
            )

            Spacer(modifier = Modifier.height(24.dp))

            val question = state.currentQuestion

            if (question != null) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = question.text,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        when (question) {
                            is MultipleListQ -> {
                                MultipleChoiceAnswer(
                                    options = question.options,
                                    selectedAnswer = state.answer,
                                    onAnswerSelected = viewModel::updateAnswer
                                )
                            }

                            is OpenQ -> {
                                OpenAnswer(
                                    answer = state.answer,
                                    onAnswerChanged = viewModel::updateAnswer
                                )
                            }

                            is MatchParQuestion -> {
                                MatchAnswer(
                                    answer = state.answer,
                                    onAnswerChanged = viewModel::updateAnswer
                                )
                            }
                        }
                    }
                }

                if (state.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = state.errorMessage!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = viewModel::submitAnswer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Подтвердить ответ")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.interruptQuiz()
                        onQuizInterrupted()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Прервать тест")
                }
            } else {
                Text(
                    text = state.errorMessage
                        ?: if (state.isFinished) {
                            "Тест завершён"
                        } else {
                            "Загрузка вопроса..."
                        }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.interruptQuiz()
                        onQuizInterrupted()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Вернуться к темам")
                }
            }
        }
    }
}

@Composable
private fun MultipleChoiceAnswer(
    options: List<String>,
    selectedAnswer: String,
    onAnswerSelected: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedAnswer == option,
                    onClick = {
                        onAnswerSelected(option)
                    }
                )

                Text(text = option)
            }
        }
    }
}

@Composable
private fun OpenAnswer(
    answer: String,
    onAnswerChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = answer,
        onValueChange = onAnswerChanged,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text("Ваш ответ")
        }
    )
}

@Composable
private fun MatchAnswer(
    answer: String,
    onAnswerChanged: (String) -> Unit
) {
    Text("Введите соответствия в формате:")

    Text("1-A, 2-B, 3-C")

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = answer,
        onValueChange = onAnswerChanged,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text("Соответствия")
        }
    )
}

private fun formatTime(seconds: Long): String {
    val safeSeconds = seconds.coerceAtLeast(0L)
    val minutes = safeSeconds / 60
    val remainingSeconds = safeSeconds % 60

    return "%02d:%02d".format(
        minutes,
        remainingSeconds
    )
}
