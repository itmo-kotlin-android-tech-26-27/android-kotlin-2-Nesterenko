
package com.example.quiz.ui.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.quiz.domain.MatchParQuestion
import com.example.quiz.domain.MultipleListQ
import com.example.quiz.domain.OpenQ
import kotlinx.coroutines.delay
import kotlin.random.Random

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

    LaunchedEffect(state.isFinished, state.isInterrupted) {
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
                                MatchingAnswer(
                                    question = question,
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

                val canSubmit = when (question) {
                    is MatchParQuestion ->
                        isMatchingComplete(question, state.answer)

                    else -> true
                }

                Button(
                    onClick = viewModel::submitAnswer,
                    enabled = canSubmit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Подтвердить ответ")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
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
private fun MatchingAnswer(
    question: MatchParQuestion,
    answer: String,
    onAnswerChanged: (String) -> Unit
) {
    val leftItems = question.pairs.keys.toList()
    val rightItems = question.pairs.values.toList()

    val displayOrder = remember(question.id, question.pairs) {
        val indices = rightItems.indices.toList()
        val shuffled = indices.shuffled(Random(question.id.toInt()))

        if (shuffled.size > 1 && shuffled == indices) {
            shuffled.drop(1) + shuffled.first()
        } else {
            shuffled
        }
    }

    val selections = parseMatchingAnswer(answer)

    Text(
        text = "Варианты для сопоставления",
        style = MaterialTheme.typography.titleSmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    displayOrder.forEach { rightIndex ->
        Text(
            text = "• ${rightItems[rightIndex]}",
            modifier = Modifier.padding(vertical = 3.dp)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = "Установите соответствия",
        style = MaterialTheme.typography.titleSmall
    )

    Spacer(modifier = Modifier.height(12.dp))

    leftItems.forEachIndexed { leftIndex, leftItem ->
        var expanded by remember(question.id, leftIndex) {
            mutableStateOf(false)
        }

        val selectedIndex = selections[leftIndex]

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "${leftIndex + 1}. $leftItem",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedIndex
                            ?.let { rightItems.getOrNull(it) }
                            ?: "Выберите соответствие"
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    }
                ) {
                    displayOrder.forEach { rightIndex ->
                        val usedByAnother = selections.any {
                                (otherLeft, otherRight) ->
                            otherLeft != leftIndex &&
                                    otherRight == rightIndex
                        }

                        DropdownMenuItem(
                            text = {
                                Text(rightItems[rightIndex])
                            },
                            enabled = !usedByAnother,
                            onClick = {
                                val updated = selections.toMutableMap()

                                updated[leftIndex] = rightIndex

                                onAnswerChanged(
                                    encodeMatchingAnswer(updated)
                                )

                                expanded = false
                            }
                        )
                    }

                    if (selectedIndex != null) {
                        DropdownMenuItem(
                            text = {
                                Text("Очистить выбор")
                            },
                            onClick = {
                                val updated = selections.toMutableMap()
                                updated.remove(leftIndex)

                                onAnswerChanged(
                                    encodeMatchingAnswer(updated)
                                )

                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }

    Text(
        text = "Заполнено: ${selections.size} из ${leftItems.size}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private fun parseMatchingAnswer(
    answer: String
): Map<Int, Int> {
    if (answer.isBlank()) return emptyMap()

    val result = mutableMapOf<Int, Int>()
    val pattern = Regex("""(\d+)-([A-Z])""")

    answer.trim()
        .uppercase()
        .split(Regex("\\s+"))
        .forEach { part ->
            val match = pattern.matchEntire(part) ?: return@forEach

            val leftIndex = match.groupValues[1]
                .toIntOrNull()
                ?.minus(1)
                ?: return@forEach

            val rightIndex = match.groupValues[2][0] - 'A'

            result[leftIndex] = rightIndex
        }

    return result
}

private fun encodeMatchingAnswer(
    selections: Map<Int, Int>
): String {
    return selections.entries
        .sortedBy { it.key }
        .joinToString(" ") { (leftIndex, rightIndex) ->
            "${leftIndex + 1}-${'A' + rightIndex}"
        }
}

private fun isMatchingComplete(
    question: MatchParQuestion,
    answer: String
): Boolean {
    val selections = parseMatchingAnswer(answer)
    val count = question.pairs.size

    return selections.size == count &&
            selections.keys.all { it in 0 until count } &&
            selections.values.all { it in 0 until count } &&
            selections.values.toSet().size == count
}

private fun formatTime(seconds: Long): String {
    val safeSeconds = seconds.coerceAtLeast(0L)

    return "%02d:%02d".format(
        safeSeconds / 60,
        safeSeconds % 60
    )
}
