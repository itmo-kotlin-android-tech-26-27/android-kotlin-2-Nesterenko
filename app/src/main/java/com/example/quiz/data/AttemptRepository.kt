
package com.example.quiz.data

import com.example.quiz.domain.Attempt
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.UUID

class AttemptRepository(
    private val file: File
) {

    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    init {
        file.parentFile?.mkdirs()
    }

    fun loadAttempts(): MutableList<Attempt> {
        if (!file.exists()) {
            return mutableListOf()
        }

        val text = file.readText()

        if (text.isBlank()) {
            return mutableListOf()
        }

        val type = object :
            TypeToken<List<AttemptData>>() {}.type

        val storedAttempts: List<AttemptData> =
            gson.fromJson(text, type)
                ?: return mutableListOf()

        val attempts = storedAttempts
            .map { it.toDomain() }
            .toMutableList()

        // Старым попыткам назначаем UUID
        // и сохраняем обновлённый JSON.
        val needsMigration = storedAttempts.any { data ->
            data.id == null ||
                    runCatching {
                        UUID.fromString(data.id)
                    }.isFailure
        }

        if (needsMigration) {
            saveAttempts(attempts)
        }

        return attempts
    }

    fun saveAttempts(
        attempts: List<Attempt>
    ) {
        val storedAttempts = attempts.map {
            AttemptData.fromDomain(it)
        }

        file.writeText(
            gson.toJson(storedAttempts)
        )
    }

    fun addAttempt(
        attempt: Attempt
    ) {
        val attempts = loadAttempts()

        attempts.add(attempt)

        saveAttempts(attempts)
    }

    fun getAttempt(
        id: UUID
    ): Attempt? {
        return loadAttempts().firstOrNull {
            it.id == id
        }
    }

    fun getAttemptsForStudent(
        login: String
    ): List<Attempt> {
        return loadAttempts().filter {
            it.studentLogin.equals(
                login,
                ignoreCase = true
            )
        }
    }

    fun deleteAttempt(
        id: UUID
    ): Boolean {
        val attempts = loadAttempts()

        val removed = attempts.removeAll {
            it.id == id
        }

        if (removed) {
            saveAttempts(attempts)
        }

        return removed
    }
}
