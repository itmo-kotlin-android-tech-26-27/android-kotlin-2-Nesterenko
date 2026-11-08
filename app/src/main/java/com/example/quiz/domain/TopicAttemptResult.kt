package com.example.quiz.domain

data class TopicAttemptResult(
    val topicId: Long,
    val topicName: String,
    val score: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val timeExpiredAnswers: Int,
    val answeredCount: Int
)