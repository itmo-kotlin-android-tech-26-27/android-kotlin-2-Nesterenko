package com.example.quiz.domain

data class Topic(
    val id: Long,
    val name: String,
    val maxLevel: Int
) {
    init {
        require(id > 0) {
            "ID темы должен быть больше 0"
        }

        require(name.isNotBlank()) {
            "Название темы не может быть пустым"
        }

        require(maxLevel > 0) {
            "Количество уровней должно быть больше 0"
        }
    }
}