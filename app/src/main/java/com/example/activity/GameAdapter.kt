package com.example.activity

import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Build
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.activity.helloWorld.Companion.movement

class GameAdapter(var numberRows : Int, var numberCols : Int, var numberMines : Int, val leftMines : TextView, val game_list : RecyclerView, val countTime : TextView, val vibrator: Vibrator) : RecyclerView.Adapter<GameAdapter.GameViewHolder>(){

    //count is maintained for checking whether the user clicks the button first time.
    var count = 0
    var game_buttons = Array(0) { Array(0) { MineCell() }}

    class GameViewHolder(view : View) : ViewHolder(view){
        val game_button = view.findViewById<Button>(R.id.button)
        val game_bomb = view.findViewById<ImageButton>(R.id.bomb)
        val game_flag = view.findViewById<ImageButton>(R.id.flag)
    }

    override fun getItemCount(): Int {
        return game_buttons.size * game_buttons[0].size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val itemLayout = LayoutInflater.from(parent.context).inflate(R.layout.game_format, parent, false)
        return GameViewHolder(itemLayout)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        with(holder){

            val start = (position) / numberCols
            val end = position - (numberCols * start)
            //When user clicks any button on board.
            game_button.setOnClickListener {
                val text = game_buttons[start][end].value
                if (game_buttons[start][end].have_mine && count == 0) {
                    count_down_timer.start()
                    helloWorld.producing_bomb(numberRows, numberCols, numberMines, game_buttons, start, end)
                    reveal(start, end)
                }

                //when user clicks first time on the game board.
                else if(count == 0){
                    helloWorld.producing_bomb(numberRows, numberCols, numberMines, game_buttons, start, end)
                    //to make sure that the first click is made on the block having no value.
                    while(game_buttons[start][end].value != 0){
                        helloWorld.reset(numberRows,numberCols, game_buttons)
                        helloWorld.producing_bomb(numberRows, numberCols, numberMines, game_buttons, start, end)
                    }
                    //starting timer when the first button is clicked.
                    count_down_timer.start()
                    //for revealing neighbour blocks.
                    reveal(start, end)
                }
                //If the button clicked is neither marked nor have mine then that block can be revealed.
                else if(!game_buttons[start][end].have_mine && !game_buttons[start][end].isMarked && !game_buttons[start][end].isRevealed){
                    reveal(start, end)
                }
                //If the button clicked have mine then user loose the game.
                else if(game_buttons[start][end].have_mine){
                    if (Build.VERSION.SDK_INT >= 26) {
                        vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                    }
                    //Timer is stopped after the user lost the game.
                    count_down_timer.cancel()
                    helloWorld.flag_bomb += 1
                    Toast.makeText(game_button.context, "You Lose !! Keep Trying...", Toast.LENGTH_SHORT).show()
                    reveal_completely()
                }
                count++
                //All buttons are disabled after they are revealed.
                game_button.isClickable = false
                game_button.isLongClickable = false
                game_bomb.isClickable = false
                game_flag.isClickable = false
                game_flag.isLongClickable = false
            }

            //for flagging the button if it clicked for long time.
            game_button.setOnLongClickListener(object: View.OnLongClickListener{
                override fun onLongClick(v: View?) : Boolean {
                    //For vibrating the device.
                    if (Build.VERSION.SDK_INT >= 26) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                    }
                    //for changing the left mines after each flag is done.
                    var left = Integer.parseInt(leftMines.text.toString())
                    left -= 1
                    //if more than required flag are done then a toast is shown.
                    if(left < 0){
                        Toast.makeText(game_button.context, "Cannot flag more than ${numberMines} Mines", Toast.LENGTH_SHORT).show()
                    }else {
                        game_button.isInvisible = true
                        game_bomb.isInvisible = true
                        game_flag.isVisible = true
                        game_buttons[start][end].isMarked = true
                        leftMines.text = left.toString()
                        checkWin(game_button)
                    }
                    return true
                }
            })

            //If wrong button is flagged then this is maintained if user want to unflag it.
            game_flag.setOnLongClickListener(object  : View.OnLongClickListener{
                override fun onLongClick(v: View?): Boolean {
                    if (Build.VERSION.SDK_INT >= 26) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                    }
                    game_bomb.isInvisible = true
                    game_flag.isInvisible = true
                    game_button.isInvisible = false
                    game_buttons[start][end].isMarked = false
                    //Increasing the number of mines left.
                    var left = Integer.parseInt(leftMines.text.toString())
                    left += 1
                    leftMines.text = left.toString()
                    return true
                }
            })
        }

    }

    //function for revealing the blocks.
    fun revealButtons(recyclerView: RecyclerView, x : Int, y: Int){
        //writing the position in terns of x and y.
        val pos = (x *  numberCols) + y

        //Finding the blocks which need to be revealed by position.
        val holder = recyclerView.findViewHolderForAdapterPosition(pos)
        if(holder != null) {
            val button_view = holder.itemView.findViewById<Button>(R.id.button)
            val bomb_view = holder.itemView.findViewById<ImageButton>(R.id.bomb)
            val flag_view = holder.itemView.findViewById<ImageButton>(R.id.flag)
            val text = game_buttons[x][y].value
            if (text == -1) {
                button_view.isVisible = false
                bomb_view.isVisible = true
                flag_view.isVisible = false
            }
            else if (text == 0) {
                button_view.setBackgroundColor(Color.parseColor("#C0C0C0"))
            }
            //setting different colour for different numbers.
            else if(text == 1){
                bomb_view.isVisible = false
                flag_view.isVisible = false
                button_view.isVisible = true
                button_view.text = text.toString()
                button_view.setTextColor(Color.parseColor("#9400D3"))
                button_view.setBackgroundColor(Color.parseColor("#C0C0C0"))
            }
            else if(text == 2){
                bomb_view.isVisible = false
                flag_view.isVisible = false
                button_view.isVisible = true
                button_view.text = text.toString()
                button_view.setTextColor(Color.parseColor("#FF307332"))
                button_view.setBackgroundColor(Color.parseColor("#C0C0C0"))
            }
            else if(text == 3){
                bomb_view.isVisible = false
                flag_view.isVisible = false
                button_view.isVisible = true
                button_view.text = text.toString()
                button_view.setTextColor(Color.RED)
                button_view.setBackgroundColor(Color.parseColor("#C0C0C0"))
            }
            else if(text == 4){
                bomb_view.isVisible = false
                flag_view.isVisible = false
                button_view.isVisible = true
                button_view.text = text.toString()
                button_view.setTextColor(Color.BLACK)
                button_view.setBackgroundColor(Color.parseColor("#C0C0C0"))
            }
            else if(text == 5){
                bomb_view.isVisible = false
                flag_view.isVisible = false
                button_view.isVisible = true
                button_view.text = text.toString()
                button_view.setTextColor(Color.BLUE)
                button_view.setBackgroundColor(Color.parseColor("#C0C0C0"))
            }
            button_view.isLongClickable = false
            button_view.isClickable = false
            bomb_view.isLongClickable = false
            bomb_view.isClickable = false
            flag_view.isLongClickable = false
            flag_view.isClickable = false
        }

    }

    //deactivating the buttons on board when the user lost the game.
    fun deactivate_buttons(recyclerView : RecyclerView){
        for(i in 0 until numberRows){
            for(j in 0 until numberCols){
                val pos = (i * numberCols) + j
                val holder = recyclerView.findViewHolderForAdapterPosition(pos)
                if(holder != null){
                    val button_view = holder.itemView.findViewById<Button>(R.id.button)
                    val bomb_view = holder.itemView.findViewById<ImageButton>(R.id.bomb)
                    val flag_view = holder.itemView.findViewById<ImageButton>(R.id.flag)
                    button_view.isLongClickable = false
                    button_view.isClickable = false
                    bomb_view.isLongClickable = false
                    bomb_view.isClickable = false
                    flag_view.isLongClickable = false
                    flag_view.isClickable = false
                }
            }
        }
    }

    //reveal function for revealing the neighbouring elements.
    private fun reveal(x: Int, y: Int)
    {
        revealButtons(game_list, x, y)
        if(!game_buttons[x][y].isRevealed && !game_buttons[x][y].isMarked){
            game_buttons[x][y].isRevealed = true
            if (game_buttons[x][y].value == 0) {
                for (i in movement)
                    for (j in movement)
                        if ((i != 0 || j != 0) && ((x + i) in 0 until numberRows) && ((y + j) in 0 until numberCols))
                            reveal((x + i), (y + j))
            }
        }
    }

    //for revealing the blocks completely when user lost the game.
    private fun reveal_completely()
    {
        for (i in 0 until numberRows)
            for (j in 0 until numberCols)
                revealButtons(game_list, i, j)
    }

    //cheking that whether user have won or not.
    private fun checkWin(game_button : Button){
        var count = 0
        for(i in 0 until numberRows){
            for(j in 0 until numberCols){
                if(game_buttons[i][j].isMarked && game_buttons[i][j].have_mine){
                    count++
                }
            }
        }

        if(count == numberMines){
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
            }
            Toast.makeText(game_button.context, "Yayyy!! You Won...", Toast.LENGTH_LONG).show()
            deactivate_buttons(game_list)
            helloWorld.flag += 1
            count_down_timer.cancel()
        }
    }

    //for maintaining timer.
    var count_down_timer : CountDownTimer = object : CountDownTimer(1000000000000, 1000){
        override fun onTick(millisUntilFinished: Long) {
            countTime.text = helloWorld.counter.toString()
            helloWorld.counter++
        }
        override fun onFinish() {
            countTime.text = "${helloWorld.counter}"
        }
    }
}
