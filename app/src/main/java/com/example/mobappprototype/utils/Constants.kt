package com.example.mobappprototype.utils

import com.example.mobappprototype.model.Question

object Constants {

    const val SEARCHED_TEXT = "searched_text"
    const val TOTAL_QUESTIONS = "total_questions"
    const val SCORE = "correct_answers"

    fun getQuestions(): MutableList<Question> {
        val questions = mutableListOf<Question>()

        val quest1 = Question(
            1, "What is the square root of 49?",
            "5", "6", "7", "8", 3
        )
        questions.add(quest1)
        val quest2 = Question(
            2, "What is the square root of 81?",
            "5", "6", "7", "9", 4
        )
        questions.add(quest2)
        val quest3 = Question(
            3, "What is the square root of 100?",
            "5", "6", "7", "10", 4
            )
        questions.add(quest3)
        val quest4 = Question(
            4, "What is the square root of 25?",
            "5", "6", "4", "7", 1
            )
        questions.add(quest4)
        val quest5 = Question(
            5, "What is the square root of 64?",
            "5", "6", "8", "7", 3
            )
        questions.add(quest5)
        val quest6 = Question(
            6, "What is the square root of 9?",
            "5", "6", "3", "7", 3
            )
        questions.add(quest6)
        val quest7 = Question(
            7, "What is the square root of 16?",
            "5", "6", "4", "7", 3
            )
        questions.add(quest7)
        val quest8 = Question(
            8, "What is the square root of 36?",
            "5", "6", "30", "7", 2
            )
        questions.add(quest8)
        val quest9 = Question(
            9, "What is the square root of 4?",
            "5", "6", "2", "7", 3
            )
        questions.add(quest9)
        val quest10 = Question(
            10, "What is your current score?",
            "9 so far! About to become 10\nwhen choosing this option",
            "What matters is that you learn\nfrom your mistakes!",
            "Does not matter, I passed anyway!",
            "Just end my misery, I'm a failure\nand will never achieve anything in life", 2
            )
        questions.add(quest10)
        return questions
    }
}