package com.example.mobappprototype.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mobappprototype.R
import com.example.mobappprototype.databinding.ActivityQuestionsBinding
import com.example.mobappprototype.model.Question
import com.example.mobappprototype.utils.Constants

private const val TAG = "QuestionsActivity"
class QuizActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityQuestionsBinding

    private var questionsCounter: Int = 0
    private var questionsList: MutableList<Question> = mutableListOf() // Initialize as an empty list
    private var subjectName: String = "" // To store the subject name
    private var selectedAnswer = 0
    private lateinit var currentQuestion: Question
    private var answered = false
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subjectName = intent.getStringExtra("SUBJECT_NAME") ?: ""

        Constants.getQuestions(subjectName) { questions ->
            if (questions != null) {
                questionsList = questions
                Log.d("QuestionSize", "${questionsList.size}") // Log after questionsList is populated
                showNextQuestion()
            } else {
                // Handle the case where fetching questions failed
                Toast.makeText(this, "Failed to load quiz", Toast.LENGTH_SHORT).show()
                // You might want to finish the activity or show an error message
            }
        }

        binding.toolbarQuizTitle.title = subjectName

        binding.tvOptionOne.setOnClickListener(this)
        binding.tvOptionTwo.setOnClickListener(this)
        binding.tvOptionThree.setOnClickListener(this)
        binding.tvOptionFour.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)

        binding.ibtnHomeFQuiz.setOnClickListener{
            Intent(this, StudentMainActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    private fun showNextQuestion () {

        if (questionsCounter < questionsList.size) {
            binding.btnNext.text = "NEXT"
            currentQuestion = questionsList[questionsCounter]
            resetOptions()
            val question = questionsList[questionsCounter]
            binding.progressBar.progress = questionsCounter
            binding.tvProgress.text = "Question ${questionsCounter + 1}/${binding.progressBar.max}"
            binding.tvQuestion.text = question.questionText
            binding.tvOptionOne.text = question.choices[0] // Access choices by index
            binding.tvOptionTwo.text = question.choices[1]
            binding.tvOptionThree.text = question.choices[2]
            binding.tvOptionFour.text = question.choices[3]
        }
        else {
            binding.btnNext.text = "FINISH"
            Intent(this, ResultActivity::class.java).also {
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
        options.add(binding.tvOptionOne)
        options.add(binding.tvOptionTwo)
        options.add(binding.tvOptionThree)
        options.add(binding.tvOptionFour)

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
                selectedOptionTv(textView = binding.tvOptionOne, selectedOptionNum = 1)
            }
            R.id.tvOptionTwo -> {
                selectedOptionTv(textView = binding.tvOptionTwo, selectedOptionNum = 2)
            }
            R.id.tvOptionThree -> {
                selectedOptionTv(textView = binding.tvOptionThree, selectedOptionNum = 3)
            }
            R.id.tvOptionFour -> {
                selectedOptionTv(textView = binding.tvOptionFour, selectedOptionNum = 4)
            }
            R.id.btnNext -> {
                if (!answered) {
                    if (selectedAnswer == 0) {
                        // Show pop-up dialog
                        showAnswerAlert()
                    } else {
                        checkAnswer()
                    }
                } else {
                    showNextQuestion()
                }
                selectedAnswer = 0
            }
        }
    }

    private fun showAnswerAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("No Answer Selected")
        builder.setMessage("Please select an answer before proceeding.")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
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
        val question = questionsList[questionsCounter - 1] // Adjust index

        if (selectedAnswer == question.choices.indexOf(question.correctAnswer) + 1) { // Compare with index + 1
            score++
//            highlightAnswer(selectedAnswer)
        }
        else {
            when(selectedAnswer) {
                1 -> {
                    binding.tvOptionOne.background = ContextCompat.getDrawable(
                        this, R.drawable.wrong_options_border_bg
                    )
                }
                2 -> {
                    binding.tvOptionTwo.background = ContextCompat.getDrawable(
                        this, R.drawable.wrong_options_border_bg
                    )
                }
                3 -> {
                    binding.tvOptionThree.background = ContextCompat.getDrawable(
                        this, R.drawable.wrong_options_border_bg
                    )
                }
                4 -> {
                    binding.tvOptionFour.background = ContextCompat.getDrawable(
                        this, R.drawable.wrong_options_border_bg
                    )
                }
            }
            highlightAnswer(question.choices.indexOf(question.correctAnswer) + 1)
        }
        binding.btnNext.text = "NEXT"
        showSolution()
    }
    private fun showSolution(){
        val question = questionsList[questionsCounter - 1] // Adjust index
        selectedAnswer = question.choices.indexOf(question.correctAnswer) + 1
        highlightAnswer(selectedAnswer)
    }

    private fun highlightAnswer(answer: Int) {
        when (answer) {
            1 -> {
                binding.tvOptionOne.background = ContextCompat.getDrawable(
                    this, R.drawable.correct_options_border_bg
                )
            }
            2 -> {
                binding.tvOptionTwo.background = ContextCompat.getDrawable(
                    this, R.drawable.correct_options_border_bg
                )
            }
            3 -> {
                binding.tvOptionThree.background = ContextCompat.getDrawable(
                    this, R.drawable.correct_options_border_bg
                )
            }
            4 -> {
                binding.tvOptionFour.background = ContextCompat.getDrawable(
                    this, R.drawable.correct_options_border_bg
                )
            }
        }
    }
}