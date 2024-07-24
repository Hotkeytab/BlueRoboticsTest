package com.example.blueroboticstest

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blueroboticstest.ui.theme.BlueRoboticsTestTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

                SequenceApplication(applicationContext)

        }
    }

@Composable
fun SequenceApplication(context: Context) {

    // State variables
    var currentStep by remember { mutableStateOf(0) }
    var errorCount by remember { mutableStateOf(0) }
    var generatedNumber by remember { mutableStateOf(0) }
    var countdownRemaining by remember { mutableStateOf(0) }
    var showCountdown by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isStarted by remember { mutableStateOf(false) }
    var firstStepSuccess by remember { mutableStateOf(false) }
    var numberOfClicks by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()


    // Function to generate a number with a countdown
    suspend fun generateNumberWithCountdown(): Int {
        val delayTime = Random.nextLong(5000, 10000)
        countdownRemaining = (delayTime / 1000).toInt()
        while (countdownRemaining > 0) {
            delay(1000)
            countdownRemaining--
        }
        return Random.nextInt(1, 4)
    }

    // Function to reset messages
    fun resetMessages() {
        errorMessage = null
        successMessage = null
    }

    // Function to reset sequence
    fun resetSequence() {
        isStarted = false
        resetMessages()
        currentStep = 0
        errorCount = 0
        numberOfClicks = 0
        firstStepSuccess = false

    }

    // Function to handle errors
    fun handleError(message: String) {
        errorCount++
        numberOfClicks++
        errorMessage = message
        if (errorCount >= 3 || numberOfClicks >= 3) {
            resetSequence()
            errorMessage = "Three errors, restart by pressing Start."
        }
    }

    // Function to handle success
    fun handleSuccess() {
        Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show()
        resetSequence();
    }

    // Function to handle sequence
    fun handleSequence(button: Int) {
        if (button == 3) {
            scope.launch {
                showCountdown = true
                generatedNumber = generateNumberWithCountdown()
                showCountdown = false
                resetMessages()
                firstStepSuccess = true
                successMessage = "Press button $generatedNumber."
            }
        } else {
            currentStep++
        }
    }

    // Function to handle button click
    fun handleButtonClick(button: Int) {
        resetMessages()
        if (!isStarted)
        {
            handleError("Please press Start")
            return
        }
        when {
            firstStepSuccess && button == generatedNumber -> handleSuccess()
            firstStepSuccess && button != generatedNumber && numberOfClicks < 3 -> handleError("Error: Press button $generatedNumber.")
            button == (currentStep + 1) -> handleSequence(button)
            else -> handleError("Error: Press button ${currentStep + 1}.")
        }
    }

    fun initializeButton(buttonName: String) {
        isStarted = buttonName == "Start"
        resetMessages()
        currentStep = 0
        errorCount = 0
        numberOfClicks = 0
        firstStepSuccess = false
    }

    // UI layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                initializeButton("Start")
            }) {
                Text("Start")
            }

            Button(onClick = {
                initializeButton("Stop")
            }) {
                Text("Stop")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(1, 2, 3).forEach { number ->
                Button(onClick = { handleButtonClick(number) }) {
                    Text(number.toString())
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        successMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
        }

        if (showCountdown) {
            Text("Countdown: $countdownRemaining")
        }
    }
}
}
