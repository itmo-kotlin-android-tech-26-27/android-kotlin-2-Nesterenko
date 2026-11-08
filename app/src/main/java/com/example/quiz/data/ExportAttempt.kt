package com.example.quiz.data

import com.example.quiz.domain.AttemptStatus

data class ExportAttempt(
    val studentLogin: String,
    val topicId: Long?,
    val topicName: String,
    val startedAt: String,
    val finishedAt: String?,
    val status: AttemptStatus,
    val score: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val topicResults: List<ExportTopicResult>,
    val answers: List<ExportAnswer>
)