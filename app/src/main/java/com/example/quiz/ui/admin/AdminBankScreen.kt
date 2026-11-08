
package com.example.quiz.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.quiz.domain.MatchParQuestion
import com.example.quiz.domain.MultipleListQ
import com.example.quiz.domain.OpenQ
import com.example.quiz.domain.Question
import com.example.quiz.domain.Topic

@Composable
fun AdminBankScreen(
    topics: List<Topic>,
    questions: List<Question>,
    onBackClick: () -> Unit,
    errorMessage: String? = null
) {
    var selectedTopicId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    var selectedQuestionId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    val selectedTopic = topics.firstOrNull {
        it.id == selectedTopicId
    }

    val selectedQuestion = questions.firstOrNull {
        it.id == selectedQuestionId &&
                it.topicId == selectedTopicId
    }

    fun goBack() {
        when {
            selectedQuestionId != null -> {
                selectedQuestionId = null
            }
            selectedTopicId != null -> {
                selectedTopicId = null
            }
            else -> onBackClick()
        }
    }

    BackHandler(
        enabled = selectedTopicId != null
    ) {
        goBack()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = when {
                        selectedQuestion != null -> "Вопрос"
                        selectedTopic != null -> selectedTopic.name
                        else -> "Банк вопросов"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )

                OutlinedButton(
                    onClick = ::goBack
                ) {
                    Text("Назад")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                when {
                    errorMessage != null -> {
                        item {
                            Text(
                                text = "Ошибка загрузки: $errorMessage",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    selectedQuestion != null -> {
                        item {
                            QuestionDetails(selectedQuestion)
                        }
                    }

                    selectedTopic != null -> {
                        val topicQuestions = questions.filter {
                            it.topicId == selectedTopic.id
                        }

                        item {
                            Text(
                                text = "Вопросов: ${topicQuestions.size}"
                            )
                        }

                        if (topicQuestions.isEmpty()) {
                            item {
                                Text("В этой теме пока нет вопросов")
                            }
                        }

                        items(
                            items = topicQuestions,
                            key = { it.id }
                        ) { question ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedQuestionId = question.id
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = question.text,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Text(
                                        text = "Уровень: ${question.level}",
                                        modifier = Modifier.padding(top = 8.dp)
                                    )

                                    Text(
                                        text = questionType(question),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        if (topics.isEmpty()) {
                            item {
                                Text("В банке пока нет тем")
                            }
                        }

                        items(
                            items = topics,
                            key = { it.id }
                        ) { topic ->
                            val count = questions.count {
                                it.topicId == topic.id
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedTopicId = topic.id
                                        selectedQuestionId = null
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = topic.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Text(
                                        text = "Уровней сложности: ${topic.maxLevel}"
                                    )

                                    Text(
                                        text = "Вопросов: $count"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionDetails(
    question: Question
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("ID: ${question.id}")
            Text("Уровень: ${question.level}")
            Text("Тип: ${questionType(question)}")

            Text(
                text = question.text,
                style = MaterialTheme.typography.titleMedium
            )

            when (question) {
                is MultipleListQ -> {
                    Text("Варианты ответа:")

                    question.options.forEachIndexed { index, option ->
                        Text("${index + 1}. $option")
                    }

                    Text(
                        text = "Правильный ответ: ${question.correctAnswer}"
                    )
                }

                is OpenQ -> {
                    Text(
                        text = "Правильный ответ: ${question.correctAnswer}"
                    )
                }

                is MatchParQuestion -> {
                    Text("Правильные соответствия:")

                    question.pairs.forEach { (left, right) ->
                        Text("$left — $right")
                    }
                }
            }
        }
    }
}

private fun questionType(question: Question): String {
    return when (question) {
        is MultipleListQ -> "Тестовый"
        is OpenQ -> "Открытый"
        is MatchParQuestion -> "Соответствие"
    }
}
