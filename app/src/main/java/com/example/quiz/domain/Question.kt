package com.example.quiz.domain

sealed class Question(
    val id: Long,
    val topicId: Long,
    val level: Int,
    val text: String
) {
    init {
        require(id > 0) {
            "ID вопроса должен быть больше 0"
        }

        require(topicId > 0) {
            "ID темы должен быть больше 0"
        }

        require(level > 0) {
            "Уровень сложности должен быть больше 0"
        }

        require(text.isNotBlank()) {
            "Текст вопроса не может быть пустым"
        }
    }
}