package com.example.ai_ml_lab_tic_tac_toe_game

import android.content.res.AssetFileDescriptor
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class TicTacToeAI {

    private val interpreter: Interpreter

    init {
        val assetFileDescriptor: AssetFileDescriptor =
            App.context.assets.openFd("ppo_tictactoe.tflite")
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        interpreter = Interpreter(modelBuffer)
    }

    fun predictMove(board: IntArray): Int {
        val input = ByteBuffer.allocateDirect(4 * 9).order(ByteOrder.nativeOrder())
        board.forEach { input.putFloat(it.toFloat()) }
        val output = ByteBuffer.allocateDirect(4 * 9).order(ByteOrder.nativeOrder())
        interpreter.run(input, output)

        val results = FloatArray(9)
        output.rewind()
        output.asFloatBuffer().get(results)

        return board.indices
            .filter { board[it] == 0 }
            .maxByOrNull { results[it] } ?: -1
    }
}
