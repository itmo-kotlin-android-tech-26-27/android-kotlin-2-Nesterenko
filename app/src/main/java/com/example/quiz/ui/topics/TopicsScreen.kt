package com.example.quiz.ui.topics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.quiz.domain.Topic

@Composable
fun TopicsScreen(
    topics: List<Topic>,
    onTopicClick: (Topic) -> Unit,
    onHistoryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "Темы",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = onHistoryClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text(
                text = "История попыток"
            )
        }

        if (topics.isEmpty()) {
            Text(
                text = "Доступных тем пока нет",
                modifier = Modifier.padding(top = 24.dp)
            )

            return
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp),
            contentPadding =
                PaddingValues(bottom = 24.dp)
        ) {
            items(
                items = topics,
                key = { topic ->
                    topic.id
                }
            ) { topic ->
                TopicItem(
                    topic = topic,
                    onClick = {
                        onTopicClick(topic)
                    }
                )
            }
        }
    }
}

@Composable
private fun TopicItem(
    topic: Topic,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = topic.name,
                style =
                    MaterialTheme.typography.titleMedium
            )

            Text(
                text =
                    "Уровней сложности: ${topic.maxLevel}",
                modifier =
                    Modifier.padding(top = 4.dp)
            )
        }
    }
}