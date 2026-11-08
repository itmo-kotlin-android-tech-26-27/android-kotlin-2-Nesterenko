package com.example.quiz.domain

data class TestSettings(
    val timeLimitMinutes: Int,
    val maxQuestionsPerTopic: Int
) {
    init {
        require(timeLimitMinutes > 0) {
            "Лимит времени должен быть больше 0"
        }

        require(maxQuestionsPerTopic > 0) {
            "Лимит вопросов должен быть больше 0"
        }
    }
}