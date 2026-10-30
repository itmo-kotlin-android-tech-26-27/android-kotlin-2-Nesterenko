package com.example.quiz.data

import com.example.quiz.domain.Attempt
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

class AttemptRepository(
    private val file: File
) {

    private val gson =
        GsonBuilder()
            .setPrettyPrinting()
            .create()

    init {
        file.parentFile?.mkdirs()
    }

    fun loadAttempts(): MutableList<Attempt> {
        if (!file.exists()) {
            return mutableListOf()
        }

        val text =
            file.readText()

        if (text.isBlank()) {
            return mutableListOf()
        }

        val type =
            object :
                TypeToken<List<AttemptData>>() {}.type

        val storedAttempts: List<AttemptData> =
            gson.fromJson(
                text,
                type
            )

        return storedAttempts
            .map { attempt ->
                attempt.toDomain()
            }
            .toMutableList()
    }

    fun saveAttempts(
        attempts: List<Attempt>
    ) {
        val storedAttempts =
            attempts.map { attempt ->
                AttemptData.fromDomain(attempt)
            }

        file.writeText(
            gson.toJson(storedAttempts)
        )
    }

    fun addAttempt(
        attempt: Attempt
    ) {
        val attempts =
            loadAttempts()

        attempts.add(attempt)

        saveAttempts(attempts)
    }

    fun getAttemptsForStudent(
        login: String
    ): List<Attempt> {
        return loadAttempts().filter { attempt ->
            attempt.studentLogin.equals(
                login,
                ignoreCase = true
            )
        }
    }

    fun deleteAttempt(
        index: Int
    ): Boolean {
        val attempts =
            loadAttempts()

        if (index !in attempts.indices) {
            return false
        }

        attempts.removeAt(index)

        saveAttempts(attempts)

        return true
    }
}