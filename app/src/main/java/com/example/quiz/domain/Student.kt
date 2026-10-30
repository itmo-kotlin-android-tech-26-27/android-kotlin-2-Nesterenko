package com.example.quiz.domain

data class Student(
    val login: String
) {
    init {
        require(login.isNotBlank()) {
            "Логин не может быть пустым"
        }
    }
}
