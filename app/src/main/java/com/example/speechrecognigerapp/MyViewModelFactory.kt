import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.speechrecognigerapp.MyViewModel
import com.example.speechrecognigerapp.TranslationService

class MyViewModelFactory(private val translationService: TranslationService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyViewModel(translationService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
