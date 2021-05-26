package com.example.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Vibrator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text
import kotlin.random.Random
import kotlin.system.exitProcess

class helloWorld : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_game)
        val countTime: TextView = findViewById(R.id.time_taken)

        //Maintained for vibrating the device when user flag any button and when game is won or lost.
        val vibrator : Vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        val restart = findViewById<Button>(R.id.restart)
        //When restart button is clicked the user again moved to the front page.
        //User will not be able to re-start the game if it is not finished.
        restart.setOnClickListener {
            if(flag != 0 || flag_bomb != 0) {
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("BEST", 1)
                }
                startActivity(intent)
            }
            else{
                Toast.makeText(this, "Complete this Game First", Toast.LENGTH_SHORT).show()
            }
        }

        //Got the number of rows, columns and mines through activity.
        val bundle : Bundle? = intent.extras
        val number_rows = bundle!!.getInt("ROWS")
        val number_cols = bundle!!.getInt("COLS")
        val number_mines = bundle!!.getInt("MINES")

        val num_buttons = Array(number_rows) { Array(number_cols) { MineCell() }}
        val leftMines = findViewById<TextView>(R.id.left_mines)
        leftMines.text = number_mines.toString()

        //used Recycler view.
        val game_list = findViewById<RecyclerView>(R.id.game)
        game_list.apply {
            layoutManager = GridLayoutManager(this@helloWorld, number_cols)

            adapter = GameAdapter(number_rows, number_cols, number_mines, leftMines, game_list, countTime, vibrator).apply {
                setHasStableIds(true)
            }
            setHasFixedSize(true)
        }
        (game_list.adapter as GameAdapter).game_buttons = num_buttons
    }

    //When user press the back button then the app is closed and if we open it again then the app will restart form the left condition.
    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    //For using the properties in other classes.
    companion object {
        //for counting the number of seconds.
        var counter = 0
        //If the user clicks on any bomb then flag_bomb will be updated to 1
        var flag_bomb = 0
        //If the user has won the game then flag will be updated to 1
        var flag = 0
        //For checking which radio button is clicked so that we can show that for which type of toughness the best time and last time is there.
        var flag_easy = false
        var flag_tough = false
        var flag_medium = false
        //For moving to neighbouring blocks.
        val movement = intArrayOf(-1, 0, 1)

        //For placing bombs randomly.
        fun producing_bomb(number_rows: Int, number_cols: Int, number_mines: Int, num_buttons: Array<Array<MineCell>>, start : Int, end : Int) {
            var i = 0
            while (i < number_mines) {
                val first = rand(0, number_rows - 1)
                val second = rand(0, number_cols - 1)
                if (!num_buttons[first][second].have_mine && first != start && second != end) {
                    num_buttons[first][second].have_mine = true
                    num_buttons[first][second].value = -1
                    updateNeighbours(number_rows, number_cols, num_buttons, first, second)
                    i += 1
                }
            }
        }

        //Random function for generating bombs at random position in every game.
        fun rand(start: Int, end: Int): Int {
            return Random(System.nanoTime()).nextInt(end - start + 1) + start
        }

        //Neighbours are updated according to the bombs placed.
        fun updateNeighbours(number_rows: Int, number_cols: Int, num_buttons: Array<Array<MineCell>>, row: Int, column: Int) {
            for (i in movement) {
                for (j in movement) {
                    if(((row+i) in 0 until number_rows) && ((column+j) in 0 until number_cols) && !num_buttons[row+i][column+j].have_mine)
                        num_buttons[row+i][column+j].value++
                }
            }
        }

        //for reseting the board.
        fun reset(number_rows: Int, number_cols: Int, num_buttons: Array<Array<MineCell>>){
            for(i in 0 until number_rows){
                for(j in 0 until number_cols){
                    num_buttons[i][j].isRevealed = false
                    num_buttons[i][j].have_mine = false
                    num_buttons[i][j].isMarked = false
                    num_buttons[i][j].value = 0
                }
            }
        }
    }
}