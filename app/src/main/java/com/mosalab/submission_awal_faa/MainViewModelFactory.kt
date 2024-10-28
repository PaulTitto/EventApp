import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mosalab.submission_awal_faa.Data.AppDatabase
import com.mosalab.submission_awal_faa.MainViewModel
import com.mosalab.submission_awal_faa.PreferencesManager

class MainViewModelFactory(
    private val database: AppDatabase,
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(database, preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
