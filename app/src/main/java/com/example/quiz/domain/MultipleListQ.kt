package com.example.quiz.domain

class MultipleListQ(
    id: Long,
    topicId: Long,
    level: Int,
    text: String,
    val options: List<String>,
    val correctAnswer: String
) : Question(
    id = id,
    topicId = topicId,
    level = level,
    text = text
) {
    init {
        require(options.size >= 2) {
            "Должно быть минимум два варианта ответа"
        }

        require(options.all { it.isNotBlank() }) {
            "Варианты ответа не могут быть пустыми"
        }

        require(correctAnswer in options) {
            "Правильный ответ должен находиться среди вариантов"
        }
    }
}