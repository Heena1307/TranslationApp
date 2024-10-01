package com.example.speechrecognigerapp

import MyViewModelFactory
import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.speechrecognigerapp.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    private var translationService: TranslationService? = null
    private var isServiceBound = false
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var myViewModel: MyViewModel

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("Hellooooooo", "onServiceConnected: service connected")
            val binder = service as TranslationService.TranslatorBinder
            translationService = binder.getService()
            isServiceBound = true
            Toast.makeText(this@MainActivity, "Service Connected", Toast.LENGTH_SHORT).show()

            // Initialize the ViewModel here after the service is bound
            myViewModel = ViewModelProvider(this@MainActivity, MyViewModelFactory(translationService!!)).get(MyViewModel::class.java)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Toast.makeText(this@MainActivity, "Ready for speech", Toast.LENGTH_SHORT).show()
                }

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {}

                override fun onError(error: Int) {
                    Toast.makeText(this@MainActivity, "Error recognizing speech: $error", Toast.LENGTH_SHORT).show()
                }

                override fun onResults(results: Bundle?) {
                    val recognizedText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                    binding.editTextInput.setText(recognizedText)

                    if (!recognizedText.isNullOrEmpty()) {
                        translateText(recognizedText)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        // Bind the com.example.speechrecognigerapp.TranslationService
        Intent(this, TranslationService::class.java).also {
            bindService(it, serviceConnection, BIND_AUTO_CREATE)
        }

        // Button click listener for starting speech recognition
        binding.btnSpeechRecognition.setOnClickListener {
            startSpeechRecognition()
        }

        // Button click listener for translation
        binding.btnTranslate.setOnClickListener {
            val text = binding.editTextInput.text.toString()
            if (text.isNotEmpty() && isServiceBound) {
                translateText(text)
            } else {
                binding.textViewResult.text = "Please enter text to translate."
            }
        }

        requestAudioPermission()
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
        }

        try {
            speechRecognizer.startListening(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Speech recognition failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun translateText(text: String) {
        if (isServiceBound) {
            myViewModel.translate(text, onResult = { translation ->
                binding.textViewResult.text = translation
            }, onFailure = {
                binding.textViewResult.text = it
            })
        } else {
            binding.textViewResult.text = "Translation service is not available."
        }
    }

    private fun requestAudioPermission() {
        val permission = Manifest.permission.RECORD_AUDIO
        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, permission) -> {
                // Show rationale to the user
                Toast.makeText(this, "Microphone permission is needed for speech recognition", Toast.LENGTH_LONG).show()
            }
            else -> {
                // Request permission
                ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission was granted
            } else {
                Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
        speechRecognizer.destroy()  // Release the SpeechRecognizer resources
    }
}
