package com.example.quiz.data

import android.util.AtomicFile
import com.example.quiz.domain.Attempt
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException
import java.util.UUID

class AttemptRepository(
    private val file: File
) {
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    private val atomicFile = AtomicFile(file)

    init {
        file.parentFile?.mkdirs()
    }

    @Synchronized
    fun loadAttempts(): MutableList<Attempt> {
        // AtomicFile поддерживает восстановление из резервной копии .bak.
        // В android.util.AtomicFile нет метода exists().
        if (!file.exists() && !File(file.path + ".bak").exists()) {
            return mutableListOf()
        }

        val json = atomicFile.openRead().bufferedReader(Charsets.UTF_8).use {
            it.readText()
        }

        if (json.isBlank()) {
            throw IOException("Файл попыток пуст или повреждён: ${file.name}")
        }

        val type = object : TypeToken<List<AttemptData>>() {}.type
        val storedAttempts: List<AttemptData> = gson.fromJson(json, type)
            ?: throw IOException("Некорректное содержимое ${file.name}")

        val attempts = storedAttempts.map { it.toDomain() }.toMutableList()

        val needsMigration = storedAttempts.any { data ->
            runCatching {
                UUID.fromString(data.id ?: "")
            }.isFailure
        }

        if (needsMigration) {
            saveAttempts(attempts)
        }

        return attempts
    }

    @Synchronized
    fun saveAttempts(attempts: List<Attempt>) {
        val json = gson.toJson(
            attempts.map { AttemptData.fromDomain(it) }
        )

        val stream = atomicFile.startWrite()

        try {
            stream.write(json.toByteArray(Charsets.UTF_8))
            atomicFile.finishWrite(stream)
        } catch (error: Exception) {
            atomicFile.failWrite(stream)
            throw error
        }
    }

    // Повторная запись того же UUID обновляет запись, а не создаёт дубль.
    @Synchronized
    fun addAttempt(attempt: Attempt) {
        val attempts = loadAttempts()
            .filterNot { it.id == attempt.id }
            .toMutableList()

        attempts.add(attempt)
        saveAttempts(attempts)
    }

    @Synchronized
    fun getAttempt(id: UUID): Attempt? {
        return loadAttempts().firstOrNull { it.id == id }
    }

    @Synchronized
    fun getAttemptsForStudent(login: String): List<Attempt> {
        return loadAttempts().filter {
            it.studentLogin.equals(login, ignoreCase = true)
        }
    }

    @Synchronized
    fun deleteAttempt(id: UUID): Boolean {
        val attempts = loadAttempts()
        val removed = attempts.removeAll { it.id == id }

        if (removed) {
            saveAttempts(attempts)
        }

        return removed
    }
}
