package com.example.speechrecognigerapp

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

import kotlinx.coroutines.tasks.await



class TranslationService  : Service() {

    private val binder = TranslatorBinder()
    private var translator: Translator? = null

    inner class TranslatorBinder : Binder() {
        fun getService(): TranslationService = this@TranslationService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }



    override fun onCreate() {
        super.onCreate()
        // Initialize translator options for the first time (can be dynamic as well)
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.HINDI)
            .build()

        translator = Translation.getClient(options)
    }

    override fun onDestroy() {
        super.onDestroy()
        translator?.close()  // Close the translator to prevent memory leaks
    }

    suspend fun translate(text: String): String {
        return try {
            // Ensure the model is downloaded
            translator?.downloadModelIfNeeded()?.await()

            // Perform translation and return result
            translator?.translate(text)?.await() ?: throw Exception("Translation failed")
        } catch (e: Exception) {
            throw Exception("Error during translation: ${e.message}")
        }
    }
}
