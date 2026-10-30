package com.example.quiz.domain

import java.time.LocalDateTime

data class Attempt(
    val studentLogin: String,
    val topicId: Long?,
    val topicName: String,
    val startedAt: LocalDateTime = LocalDateTime.now(),
    var finishedAt: LocalDateTime? = null,
    var status: AttemptStatus = AttemptStatus.IN_PROGRESS,
    val answers: MutableList<AnswerRecord> = mutableListOf()
) {

    val correctAnswers: Int
        get() = answers.count { answer ->
            answer.isCorrect && !answer.timedOut
        }

    val wrongAnswers: Int
        get() = answers.count { answer ->
            !answer.isCorrect && !answer.timedOut
        }

    val timeExpiredAnswers: Int
        get() = answers.count { answer ->
            answer.timedOut
        }

    val score: Int
        get() = correctAnswers

    val answeredCount: Int
        get() = answers.size

    fun addAnswer(
        answer: AnswerRecord
    ) {
        answers.add(answer)
    }

    fun finish(
        newStatus: AttemptStatus
    ) {
        status = newStatus
        finishedAt = LocalDateTime.now()
    }

    fun getTopicResults(): List<TopicAttemptResult> {
        return answers
            .groupBy { answer ->
                answer.topicId
            }
            .map { (topicId, topicAnswers) ->
                createTopicResult(
                    topicId = topicId,
                    answers = topicAnswers
                )
            }
    }

    private fun createTopicResult(
        topicId: Long,
        answers: List<AnswerRecord>
    ): TopicAttemptResult {
        val topicName =
            answers.first().topicName

        val correctAnswers =
            answers.count { answer ->
                answer.isCorrect && !answer.timedOut
            }

        val wrongAnswers =
            answers.count { answer ->
                !answer.isCorrect && !answer.timedOut
            }

        val timeExpiredAnswers =
            answers.count { answer ->
                answer.timedOut
            }

        return TopicAttemptResult(
            topicId = topicId,
            topicName = topicName,
            score = correctAnswers,
            correctAnswers = correctAnswers,
            wrongAnswers = wrongAnswers,
            timeExpiredAnswers = timeExpiredAnswers,
            answeredCount = answers.size
        )
    }
}