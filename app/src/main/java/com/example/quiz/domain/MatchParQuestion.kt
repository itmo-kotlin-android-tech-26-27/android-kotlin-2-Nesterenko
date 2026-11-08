package com.example.quiz.domain

class MatchParQuestion(
    id: Long,
    topicId: Long,
    level: Int,
    text: String,
    val pairs: Map<String, String>
) : Question(
    id = id,
    topicId = topicId,
    level = level,
    text = text
) {
    init {
        require(pairs.isNotEmpty()) {
            "Список пар не может быть пустым"
        }

        require(
            pairs.keys.all { it.isNotBlank() } &&
                    pairs.values.all { it.isNotBlank() }
        ) {
            "Элементы пар не могут быть пустыми"
        }
    }
}