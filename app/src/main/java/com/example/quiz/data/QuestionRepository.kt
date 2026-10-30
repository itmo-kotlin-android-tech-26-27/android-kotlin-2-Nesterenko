package com.example.quiz.data

import com.example.quiz.domain.MatchParQuestion
import com.example.quiz.domain.MultipleListQ
import com.example.quiz.domain.OpenQ
import com.example.quiz.domain.Question
import com.example.quiz.domain.TestSettings
import com.example.quiz.domain.Topic
import com.google.gson.GsonBuilder
import java.io.File

class QuestionRepository(
    file: File
) {

    private var currentFile: File = file

    private val gson =
        GsonBuilder()
            .setPrettyPrinting()
            .create()

    init {
        currentFile.parentFile?.mkdirs()
        createDemoDataIfNeeded()
    }

    fun loadFromFile(
        file: File
    ) {
        require(file.exists()) {
            "Файл не найден"
        }

        require(file.isFile) {
            "Указанный путь не является файлом"
        }

        val data =
            readData(file)

        validateData(data)

        currentFile = file
    }

    fun getCurrentFile(): File {
        return currentFile
    }

    fun getSettings(): TestSettings {
        return loadData().settings
    }

    fun getTopics(): List<Topic> {
        return loadData().topics
    }

    fun getQuestions(): List<Question> {
        return loadData()
            .questions
            .map { question ->
                question.toDomain()
            }
    }

    fun updateSettings(
        settings: TestSettings
    ) {
        val data =
            loadData()

        saveData(
            data.copy(
                settings = settings
            )
        )
    }

    fun addTopic(
        topic: Topic
    ): Boolean {
        val data =
            loadData()

        val topicExists =
            data.topics.any { existingTopic ->
                existingTopic.id == topic.id
            }

        if (topicExists) {
            return false
        }

        saveData(
            data.copy(
                topics =
                    data.topics + topic
            )
        )

        return true
    }

    fun updateTopic(
        topicId: Long,
        newName: String,
        newMaxLevel: Int
    ): Boolean {
        val data =
            loadData()

        if (
            data.topics.none { topic ->
                topic.id == topicId
            }
        ) {
            return false
        }

        val updatedTopics =
            data.topics.map { topic ->
                if (topic.id == topicId) {
                    topic.copy(
                        name = newName,
                        maxLevel = newMaxLevel
                    )
                } else {
                    topic
                }
            }

        saveData(
            data.copy(
                topics = updatedTopics
            )
        )

        return true
    }

    fun deleteTopic(
        topicId: Long
    ): Boolean {
        val data =
            loadData()

        if (
            data.topics.none { topic ->
                topic.id == topicId
            }
        ) {
            return false
        }

        val updatedTopics =
            data.topics.filter { topic ->
                topic.id != topicId
            }

        val updatedQuestions =
            data.questions.filter { question ->
                question.topicId != topicId
            }

        saveData(
            data.copy(
                topics = updatedTopics,
                questions = updatedQuestions
            )
        )

        return true
    }

    fun addQuestion(
        question: Question
    ): Boolean {
        val data =
            loadData()

        if (
            data.questions.any { existingQuestion ->
                existingQuestion.id == question.id
            }
        ) {
            return false
        }

        val topic =
            data.topics.firstOrNull { topic ->
                topic.id == question.topicId
            } ?: return false

        if (
            question.level !in
            1..topic.maxLevel
        ) {
            return false
        }

        saveData(
            data.copy(
                questions =
                    data.questions +
                            QuestionData.fromDomain(question)
            )
        )

        return true
    }

    fun updateQuestion(
        questionId: Long,
        updatedQuestion: Question
    ): Boolean {
        val data =
            loadData()

        if (
            data.questions.none { question ->
                question.id == questionId
            }
        ) {
            return false
        }

        val topic =
            data.topics.firstOrNull { topic ->
                topic.id == updatedQuestion.topicId
            } ?: return false

        if (
            updatedQuestion.level !in
            1..topic.maxLevel
        ) {
            return false
        }

        val updatedQuestions =
            data.questions.map { question ->
                if (question.id == questionId) {
                    QuestionData.fromDomain(
                        updatedQuestion
                    )
                } else {
                    question
                }
            }

        saveData(
            data.copy(
                questions = updatedQuestions
            )
        )

        return true
    }

    fun deleteQuestion(
        questionId: Long
    ): Boolean {
        val data =
            loadData()

        if (
            data.questions.none { question ->
                question.id == questionId
            }
        ) {
            return false
        }

        saveData(
            data.copy(
                questions =
                    data.questions.filter { question ->
                        question.id != questionId
                    }
            )
        )

        return true
    }

    private fun loadData(): QuizData {
        return readData(currentFile)
    }

    private fun readData(
        file: File
    ): QuizData {
        val data =
            gson.fromJson(
                file.readText(),
                QuizData::class.java
            )

        return requireNotNull(data) {
            "Не удалось прочитать данные из файла"
        }
    }

    private fun saveData(
        data: QuizData
    ) {
        currentFile.writeText(
            gson.toJson(data)
        )
    }

    private fun validateData(
        data: QuizData
    ) {
        val topicIds =
            data.topics
                .map { topic ->
                    topic.id
                }
                .toSet()

        data.questions.forEach { questionData ->
            val question =
                questionData.toDomain()

            require(
                question.topicId in topicIds
            ) {
                "Для вопроса ${question.id} " +
                        "не найдена тема ${question.topicId}"
            }

            val topic =
                data.topics.first { topic ->
                    topic.id == question.topicId
                }

            require(
                question.level in
                        1..topic.maxLevel
            ) {
                "Некорректный уровень вопроса ${question.id}"
            }
        }
    }

    private fun createDemoDataIfNeeded() {
        if (currentFile.exists()) {
            return
        }

        val settings =
            TestSettings(
                timeLimitMinutes = 10,
                maxQuestionsPerTopic = 5
            )

        val topics =
            listOf(
                Topic(
                    id = 1L,
                    name = "Основы Kotlin",
                    maxLevel = 3
                )
            )

        val questions =
            listOf<Question>(
                MatchParQuestion(
                    id = 7L,
                    topicId = 1L,
                    level = 1,
                    text =
                        "Сопоставьте элементы Kotlin с их назначением",
                    pairs = linkedMapOf(
                        "val" to "неизменяемая переменная",
                        "var" to "изменяемая переменная",
                        "fun" to "функция"
                    )
                ),

                MultipleListQ(
                    id = 1L,
                    topicId = 1L,
                    level = 1,
                    text =
                        "Как объявить неизменяемую переменную в Kotlin?",
                    options =
                        listOf(
                            "var",
                            "val",
                            "fun"
                        ),
                    correctAnswer = "val"
                ),

                MultipleListQ(
                    id = 2L,
                    topicId = 1L,
                    level = 1,
                    text =
                        "Как объявить изменяемую переменную в Kotlin?",
                    options =
                        listOf(
                            "val",
                            "var",
                            "let"
                        ),
                    correctAnswer = "var"
                ),

                MultipleListQ(
                    id = 3L,
                    topicId = 1L,
                    level = 2,
                    text =
                        "Какое ключевое слово используется для объявления функции?",
                    options =
                        listOf(
                            "fun",
                            "function",
                            "def"
                        ),
                    correctAnswer = "fun"
                ),

                MultipleListQ(
                    id = 4L,
                    topicId = 1L,
                    level = 2,
                    text =
                        "Какой тип используется для целых чисел?",
                    options =
                        listOf(
                            "String",
                            "Int",
                            "Boolean"
                        ),
                    correctAnswer = "Int"
                ),

                OpenQ(
                    id = 5L,
                    topicId = 1L,
                    level = 3,
                    text =
                        "Как называется тип для целых чисел в Kotlin?",
                    correctAnswer = "Int"
                ),

                MultipleListQ(
                    id = 6L,
                    topicId = 1L,
                    level = 3,
                    text =
                        "Какой оператор является Elvis-оператором?",
                    options =
                        listOf(
                            "?.",
                            "!!",
                            "?:"
                        ),
                    correctAnswer = "?:"
                )
            )

        val data =
            QuizData(
                settings = settings,
                topics = topics,
                questions =
                    questions.map { question ->
                        QuestionData.fromDomain(question)
                    }
            )

        saveData(data)
    }
}