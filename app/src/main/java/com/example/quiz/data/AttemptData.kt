package com.example.quiz.data

import com.example.quiz.domain.Attempt
import com.example.quiz.domain.AttemptStatus
import java.time.LocalDateTime

data class AttemptData(
    val studentLogin: String,
    val topicId: Long?,
    val topicName: String,
    val startedAt: String,
    val finishedAt: String?,
    val status: AttemptStatus,
    val answers: List<AnswerRecordData>
) {

    fun toDomain(): Attempt {
        return Attempt(
            studentLogin = studentLogin,
            topicId = topicId,
            topicName = topicName,
            startedAt = LocalDateTime.parse(startedAt),
            finishedAt =
                finishedAt?.let {
                    LocalDateTime.parse(it)
                },
            status = status,
            answers =
                answers
                    .map { answer ->
                        answer.toDomain()
                    }
                    .toMutableList()
        )
    }

    companion object {

        fun fromDomain(
            attempt: Attempt
        ): AttemptData {
            return AttemptData(
                studentLogin = attempt.studentLogin,
                topicId = attempt.topicId,
                topicName = attempt.topicName,
                startedAt = attempt.startedAt.toString(),
                finishedAt = attempt.finishedAt?.toString(),
                status = attempt.status,
                answers =
                    attempt.answers.map { answer ->
                        AnswerRecordData.fromDomain(answer)
                    }
            )
        }
    }
}