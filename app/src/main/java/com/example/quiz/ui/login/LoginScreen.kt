package com.example.quiz.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class UserRole {
    STUDENT,
    ADMIN
}

@Composable
fun LoginScreen(
    onStudentLogin: (String) -> Unit,
    onAdminLogin: () -> Unit
) {
    var selectedRole by remember {
        mutableStateOf(UserRole.STUDENT)
    }

    var login by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Квиз",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "Выберите роль"
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected =
                    selectedRole == UserRole.STUDENT,
                onClick = {
                    selectedRole = UserRole.STUDENT
                    errorMessage = null
                }
            )

            Text(
                text = "Студент"
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected =
                    selectedRole == UserRole.ADMIN,
                onClick = {
                    selectedRole = UserRole.ADMIN
                    errorMessage = null
                }
            )

            Text(
                text = "Администратор"
            )
        }

        if (
            selectedRole == UserRole.STUDENT
        ) {
            Spacer(
                modifier = Modifier.height(16.dp)
            )

            OutlinedTextField(
                value = login,
                onValueChange = {
                    login = it
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        text = "Логин"
                    )
                },
                singleLine = true,
                isError =
                    errorMessage != null
            )
        }

        if (
            errorMessage != null
        ) {
            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = errorMessage!!,
                color =
                    MaterialTheme.colorScheme.error
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(
            onClick = {
                when (selectedRole) {
                    UserRole.STUDENT -> {
                        val cleanLogin =
                            login.trim()

                        if (
                            cleanLogin.isBlank()
                        ) {
                            errorMessage =
                                "Введите логин"
                        } else {
                            onStudentLogin(
                                cleanLogin
                            )
                        }
                    }

                    UserRole.ADMIN -> {
                        onAdminLogin()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Войти"
            )
        }
    }
}