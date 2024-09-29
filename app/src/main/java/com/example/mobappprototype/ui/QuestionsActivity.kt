package com.example.mobappprototype.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mobappprototype.R
import com.example.mobappprototype.model.Question
import com.example.mobappprototype.utils.Constants
import com.google.android.material.button.MaterialButton

class QuestionsActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var tvQuestion: TextView

    private lateinit var tvOptionOne: TextView
    private lateinit var tvOptionTwo: TextView
    private lateinit var tvOptionThree: TextView
    private lateinit var tvOptionFour: TextView
    private lateinit var btnNext: MaterialButton

    private var questionsCounter: Int = 0
    private lateinit var questionsList: MutableList<Question>
    private var selectedAnswer = 0
    private lateinit var currentQuestion: Question
    private var answered = false
    private lateinit var name: String
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_questions)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        progressBar = findViewById(R.id.progressBar)
        tvProgress = findViewById(R.id.tvProgress)
        tvQuestion = findViewById(R.id.tvQuestion)
        btnNext = findViewById(R.id.btnNext)

        tvOptionOne = findViewById(R.id.tvOptionOne)
        tvOptionTwo = findViewById(R.id.tvOptionTwo)
        tvOptionThree = findViewById(R.id.tvOptionThree)
        tvOptionFour = findViewById(R.id.tvOptionFour)

        tvOptionOne.setOnClickListener(this)
        tvOptionTwo.setOnClickListener(this)
        tvOptionThree.setOnClickListener(this)
        tvOptionFour.setOnClickListener(this)
        btnNext.setOnClickListener(this)

        questionsList = Constants.getQuestions()
        Log.d("QuestionSize", "${questionsList.size}")

        showNextQuestion()

//        if (intent.hasExtra(Constants.USER_NAME)){
//            name = intent.getStringExtra(Constants.USER_NAME)!!
//        }
    }

    private fun showNextQuestion () {

        if (questionsCounter < questionsList.size) {
            btnNext.text = "NEXT"
            currentQuestion = questionsList[questionsCounter]

            resetOptions()
            val question = questionsList[questionsCounter]
            progressBar.progress = questionsCounter
            tvProgress.text = "Question ${questionsCounter + 1}/${progressBar.max}"
            tvQuestion.text = question.question
            tvOptionOne.text = question.optionOne
            tvOptionTwo.text = question.optionTwo
            tvOptionThree.text = question.optionThree
            tvOptionFour.text = question.optionFour
        }
        else {
            btnNext.text = "FINISH"
            Intent(this, ResultActivity::class.java).also {
//                it.putExtra(Constants.USER_NAME, name)
                it.putExtra(Constants.SCORE, score)
                it.putExtra(Constants.TOTAL_QUESTIONS, questionsList.size)
                startActivity(it)
            }
        }

        questionsCounter++
        answered = false
    }

    private fun resetOptions() {
        val options = mutableListOf<TextView>()
        options.add(tvOptionOne)
        options.add(tvOptionTwo)
        options.add(tvOptionThree)
        options.add(tvOptionFour)

        for (option in options) {
            option.setTextColor(Color.parseColor("#1E232C"))
            option.typeface = Typeface.DEFAULT
            option.background = ContextCompat.getDrawable(
                this, R.drawable.default_options_border_bg
            )
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.tvOptionOne -> {
                selectedOptionTv(textView = tvOptionOne, selectedOptionNum = 1)
            }
            R.id.tvOptionTwo -> {
                selectedOptionTv(textView = tvOptionTwo, selectedOptionNum = 2)
            }
            R.id.tvOptionThree -> {
                selectedOptionTv(textView = tvOptionThree, selectedOptionNum = 3)
            }
            R.id.tvOptionFour -> {
                selectedOptionTv(textView = tvOptionFour, selectedOptionNum = 4)
            }
            R.id.btnNext -> {
                if (!answered) {
                    checkAnswer()
                } else {
                    showNextQuestion()
                }
                selectedAnswer = 0
            }
        }
    }

    private fun selectedOptionTv(textView: TextView, selectedOptionNum: Int) {
        resetOptions()
        selectedAnswer = selectedOptionNum

        textView.setTextColor(Color.parseColor("#09593c"))
        textView.setTypeface(textView.typeface, Typeface.BOLD)
        textView.background = ContextCompat.getDrawable(
            this, R.drawable.selected_options_border_bg
        )
    }

    private fun checkAnswer() {
        answered = true

        if(selectedAnswer == currentQuestion.correctAnswer) {
            score++
            highlightAnswer(selectedAnswer)
        }
        else {
            when(selectedAnswer) {
                1 -> {
                    tvOptionOne.background = ContextCompat.getDrawable(
                        this, R.drawable.wrong_options_border_bg
                    )
                }
                2 -> {
                    tvOptionTwo.background = ContextCompat.getDrawable(
                        this, R.drawable.wrong_options_border_bg
                    )
                }
                3 -> {
                    tvOptionThree.background = ContextCompat.getDrawable(
                        this, R.drawable.wrong_options_border_bg
                    )
                }
                4 -> {
                    tvOptionFour.background = ContextCompat.getDrawable(
                        this, R.drawable.wrong_options_border_bg
                    )
                }
            }
        }
        btnNext.text = "NEXT"
        showSolution()
    }
    private fun showSolution(){
        selectedAnswer = currentQuestion.correctAnswer
        highlightAnswer(selectedAnswer)
    }

    private fun highlightAnswer(answer: Int) {
        when (answer) {
            1 -> {
                tvOptionOne.background = ContextCompat.getDrawable(
                    this, R.drawable.correct_options_border_bg
                )
            }
            2 -> {
                tvOptionTwo.background = ContextCompat.getDrawable(
                    this, R.drawable.correct_options_border_bg
                )
            }
            3 -> {
                tvOptionThree.background = ContextCompat.getDrawable(
                    this, R.drawable.correct_options_border_bg
                )
            }
            4 -> {
                tvOptionFour.background = ContextCompat.getDrawable(
                    this, R.drawable.correct_options_border_bg
                )
            }
        }
    }
}