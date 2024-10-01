package com.example.speechrecognigerapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyViewModel (private val translationService: TranslationService) : ViewModel() {

    fun translate(text: String, onResult: (String) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val translation = withContext(Dispatchers.IO) {
                    translationService.translate(text)


                }
                onResult(translation)
            } catch (e: Exception) {
                onFailure("Error occured")

            }
        }
    }


}