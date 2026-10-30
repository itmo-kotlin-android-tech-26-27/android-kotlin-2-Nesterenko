package com.example.quiz.domain

class OpenQ(
    id: Long,
    topicId: Long,
    level: Int,
    text: String,
    val correctAnswer: String
) : Question(
    id = id,
    topicId = topicId,
    level = level,
    text = text
) {
    init {
        require(correctAnswer.isNotBlank()) {
            "Правильный ответ не может быть пустым"
        }
    }
}