
package com.example.quiz.data

import com.example.quiz.domain.Attempt
import com.example.quiz.domain.AttemptStatus
import java.time.LocalDateTime
import java.util.UUID

data class AttemptData(
    val studentLogin: String,
    val topicId: Long?,
    val topicName: String,
    val startedAt: String,
    val finishedAt: String?,
    val status: AttemptStatus,
    val answers: List<AnswerRecordData>,
    val id: String? = null
) {

    fun toDomain(): Attempt {
        val attemptId = runCatching {
            id?.let { UUID.fromString(it) }
        }.getOrNull() ?: UUID.randomUUID()

        return Attempt(
            studentLogin = studentLogin,
            topicId = topicId,
            topicName = topicName,
            startedAt = LocalDateTime.parse(startedAt),
            finishedAt = finishedAt?.let {
                LocalDateTime.parse(it)
            },
            status = status,
            answers = answers
                .map { it.toDomain() }
                .toMutableList(),
            id = attemptId
        )
    }

    companion object {
        fun fromDomain(attempt: Attempt): AttemptData {
            return AttemptData(
                studentLogin = attempt.studentLogin,
                topicId = attempt.topicId,
                topicName = attempt.topicName,
                startedAt = attempt.startedAt.toString(),
                finishedAt = attempt.finishedAt?.toString(),
                status = attempt.status,
                answers = attempt.answers.map {
                    AnswerRecordData.fromDomain(it)
                },
                id = attempt.id.toString()
            )
        }
    }
}
