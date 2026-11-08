package com.example.quiz.ui.common

import com.example.quiz.domain.AttemptStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun AttemptStatus.displayName(): String = when (this) {
    AttemptStatus.IN_PROGRESS -> "В процессе"
    AttemptStatus.COMPLETED -> "Завершена"
    AttemptStatus.INTERRUPTED -> "Прервана"
    AttemptStatus.TIME_EXPIRED -> "Время вышло"
}

fun LocalDateTime.displayDate(): String =
    format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
