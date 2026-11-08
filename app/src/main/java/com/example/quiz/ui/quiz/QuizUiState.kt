
package com.example.quiz.ui.quiz

import com.example.quiz.domain.Question

data class QuizUiState(
    val studentLogin: String = "",
    val topicId: Long = 0L,
    val topicName: String = "",
    val currentLevel: Int = 1,
    val maxLevel: Int = 1,
    val remainingSeconds: Long = 0L,
    val currentQuestion: Question? = null,
    val answer: String = "",
    val isAnswerSubmitted: Boolean = false,
    val isFinished: Boolean = false,
    val isInterrupted: Boolean = false,
    val isTimeExpired: Boolean = false,
    val savedAttemptId: String? = null,
    val errorMessage: String? = null
)
