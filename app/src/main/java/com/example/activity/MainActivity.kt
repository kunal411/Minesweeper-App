package com.example.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.example.activity.R
import java.lang.Double.parseDouble

class MainActivity : AppCompatActivity() {


    var flagRadio : Boolean = false
    var flagCustom : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //find the views on the activity_main using their ids.
        val bestTime = findViewById<TextView>(R.id.best_time)
        val lastGameTime = findViewById<TextView>(R.id.last_game_time)
        val best_toughness = findViewById<TextView>(R.id.toughness)
        val last_toughness = findViewById<TextView>(R.id.last_toughness)


        var count = helloWorld.counter
        //Load the data when the app is opened so that the changes will be reflected even after closing the app.
        loadData(bestTime, lastGameTime, best_toughness, last_toughness)
        val best_time = Integer.parseInt(bestTime.text.toString())
        val last_time = Integer.parseInt(lastGameTime.text.toString())

        //To change the best time and last game time.
        //If the user won the game then flag will be greater than zero.
        //If the user loose the game then flag_bomb will be greater than zero.
        //The time will only change when the game is won.
        if(helloWorld.flag != 0 && helloWorld.flag_bomb == 0) {
            if (count < best_time || best_time == 0) {
                bestTime.text = count.toString()
                if(helloWorld.flag_easy){
                    best_toughness.text = getString(R.string.easy)
                }
                else if(helloWorld.flag_medium){
                    best_toughness.text = getString(R.string.medium)
                }
                else if(helloWorld.flag_tough){
                    best_toughness.text = getString(R.string.tough)
                }
                else{
                    best_toughness.text = "(Custom Board)"
                }
            }
            lastGameTime.text = count.toString()
            if(helloWorld.flag_easy){
                last_toughness.text = getString(R.string.easy)
            }
            else if(helloWorld.flag_medium){
                last_toughness.text = getString(R.string.medium)
            }
            else if(helloWorld.flag_tough){
                last_toughness.text = getString(R.string.tough)
            }
            else{
                last_toughness.text = "(Custom Board)"
            }
            helloWorld.flag_easy = false
            helloWorld.flag_medium = false
            helloWorld.flag_tough = false
        }

        //Intialised to 0 again for the new game.
        helloWorld.flag = 0
        helloWorld.flag_bomb = 0
        saveData(bestTime, lastGameTime, best_toughness, last_toughness)

        val startActivity : Button = findViewById(R.id.start_game)

        val radioGrp : RadioGroup = findViewById(R.id.radio)
        val customBoard : Button = findViewById(R.id.custom_board)
        val numRows : EditText = findViewById(R.id.num_row)
        val numCols : EditText = findViewById(R.id.num_col)
        val numMines : EditText = findViewById(R.id.num_mines)
        val howToPlay = findViewById<ImageButton>(R.id.how_to_play)


        //Maintained for "how to play" guide for user.
        howToPlay.setOnClickListener {
            val intent = Intent(this, Howto :: class.java).apply {
                putExtra("abc", 1)
            }
            startActivity(intent)
        }

        //If user clicks on any of the radio buttons then the "Make custom board" is deselected.
        radioGrp.setOnCheckedChangeListener{group, checkedId ->
            numRows.isInvisible = true
            numCols.isInvisible = true
            numMines.isInvisible = true
            numRows.text.clear()
            numCols.text.clear()
            numMines.text.clear()
            flagRadio = true
            flagCustom = false
        }


        customBoard.setOnClickListener {
            //if the user clicks on the "Make custom Board" then the radio buttons get deselected.
            radioGrp.findViewById<RadioButton>(R.id.easy).isChecked = false
            radioGrp.findViewById<RadioButton>(R.id.medium).isChecked = false
            radioGrp.findViewById<RadioButton>(R.id.tough).isChecked = false
            numRows.isVisible = true
            numCols.isVisible = true
            numMines.isVisible = true
            flagCustom = true
            flagRadio = false;

            //These lines indicates the user for entering the numbers in the appropriate range.
            setErrorListener(numMines, "Number of mines should be less than 1/5th of number of rows * number of cols")
            setErrorListener(numRows, "Number of rows should be less than or equal to 10")
            setErrorListener(numCols, "Number of columns should be less than or equal to 10")
        }


        //When user clicks on Start Button.
        startActivity.setOnClickListener {
            //If the custom board is selected.
            if(flagCustom) {
                //checking whether or not the numbers are in the told range.
                if (numRows.text.toString() != "" && numCols.text.toString() != "" && numMines.text.toString() != "" && checkNum(numRows.text.toString()) && checkNum(numCols.text.toString()) && checkNum(numMines.text.toString())) {
                    val number_rows = Integer.parseInt(numRows.text.toString())
                    val number_cols = Integer.parseInt(numCols.text.toString())
                    val number_mines = Integer.parseInt(numMines.text.toString())
                    if ((number_rows in 1..10) && (number_cols in 1..10) && (number_mines in 1..(number_rows * number_cols) / 5)) {
                        helloWorld.flag_easy = false
                        helloWorld.flag_medium = false
                        helloWorld.flag_tough = false
                        otherActivity(number_rows, number_cols, number_mines)
                    }
                    else {
                        Toast.makeText(this, "Enter numbers in the shown range", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Toast.makeText(this, "Enter valid details", Toast.LENGTH_SHORT).show()
                }
            }

            //When radio buttons are selected.
            else if(flagRadio){
                //If easy radio button is selected.
                if(radioGrp.findViewById<RadioButton>(R.id.easy).isChecked){
                    helloWorld.flag_easy = true
                    otherActivity(10, 8, 5)
                }
                //If the medium radio button is selected.
                else if(radioGrp.findViewById<RadioButton>(R.id.medium).isChecked){
                    helloWorld.flag_medium = true
                    otherActivity(10, 8, 10)
                }
                //If the tough radio button is selected.
                else if(radioGrp.findViewById<RadioButton>(R.id.tough).isChecked){
                    helloWorld.flag_tough = true
                    otherActivity(10, 8, 15)
                }
            }
            else{
                //When user has not selected any button.
                Toast.makeText(this, "Select Board", Toast.LENGTH_SHORT).show()
            }
            helloWorld.counter = 0
        }
    }

    //When user press the back button then the app is closed and if we open it again then the app will restart form the left condition.
    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    //Funtcion to move to other activity where user will play the game.
    private fun otherActivity(num_rows : Int, num_cols : Int, num_mines : Int){
        val intent = Intent(this, helloWorld::class.java).apply {
            putExtra("ROWS", num_rows)
            putExtra("COLS", num_cols)
            putExtra("MINES", num_mines)
        }
        startActivity(intent)
    }

    //For showing the warning for entering numbers in the appropriate range.
    private fun setErrorListener(editText : TextView, message : String){
        editText.error = if(editText.text.toString().isNotEmpty()) null else message
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                editText.error = if(editText.text.toString().isNotEmpty()) null else message
            }
        })
    }

    //Best time and last game time will be saved after each game.
    private fun saveData(bestTime : TextView, lastGame : TextView, best_tough : TextView, last_tough : TextView){
        val best_time = bestTime.text.toString()
        val last_game_time = lastGame.text.toString()
        val best_toughness = best_tough.text.toString()
        val last_toughness = last_tough.text.toString()
        val sharedPreferences = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.apply{
            putString("BEST", best_time)
            putString("LAST", last_game_time)
            putString("Best_tough", best_toughness)
            putString("Last_tough", last_toughness)
        }.apply()
    }

    //Loading the best time and last game time after re-opening the app.
    private fun loadData(bestTime : TextView, lastGame : TextView, best_tough : TextView, last_tough : TextView){
        val sharedPreferences = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        val best_time = sharedPreferences.getString("BEST", "0")
        val last_game_time = sharedPreferences.getString("LAST", "0")
        val best_toughness = sharedPreferences.getString("Best_tough", "(Board Type)")
        val last_toughness = sharedPreferences.getString("Last_tough", "(Board Type)")

        bestTime.text = best_time
        lastGame.text = last_game_time
        best_tough.text = best_toughness
        last_tough.text = last_toughness
    }

    //To check whether the characters entered are digits or not.
    private fun checkNum(str : String) : Boolean{
        var numeric = true

        try {
            val num = parseDouble(str)
        } catch (e: NumberFormatException) {
            numeric = false
        }

        if(numeric){
            return true
        }
        return false
    }
}