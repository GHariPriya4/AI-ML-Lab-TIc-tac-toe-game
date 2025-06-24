package com.example.ai_ml_lab_tic_tac_toe_game

import android.content.res.AssetFileDescriptor
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {

    private lateinit var interpreter: Interpreter
    private lateinit var buttons: Array<Button>
    private var board = IntArray(9) { 0 } // 0 = empty, 1 = player, 2 = AI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            interpreter = Interpreter(loadModelFile("ppo_tictactoe.tflite"))
        } catch (e: Exception) {
            Toast.makeText(this, "Model loading failed: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
            return
        }

        setupBoard()
    }

    private fun setupBoard() {
        buttons = Array(9) { i ->
            val resId = resources.getIdentifier("btn$i", "id", packageName)
            findViewById(resId)
        }

        for (i in buttons.indices) {
            buttons[i].setOnClickListener {
                if (board[i] == 0) {
                    board[i] = 1
                    buttons[i].text = "X"
                    buttons[i].isEnabled = false
                    aiMove()
                }
            }
        }
    }

    private fun aiMove() {
        val input = Array(1) { FloatArray(9) }
        for (i in board.indices) {
            input[0][i] = board[i].toFloat()
        }

        val output = Array(1) { FloatArray(9) }
        interpreter.run(input, output)

        var bestIndex = -1
        var bestScore = Float.NEGATIVE_INFINITY
        for (i in output[0].indices) {
            if (board[i] == 0 && output[0][i] > bestScore) {
                bestScore = output[0][i]
                bestIndex = i
            }
        }

        if (bestIndex != -1) {
            board[bestIndex] = 2
            buttons[bestIndex].text = "O"
            buttons[bestIndex].isEnabled = false
        }
    }

    private fun loadModelFile(modelName: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}
