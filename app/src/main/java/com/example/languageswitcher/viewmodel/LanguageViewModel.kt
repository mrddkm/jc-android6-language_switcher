package com.example.languageswitcher.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.languageswitcher.R
import com.example.languageswitcher.model.Language
import com.example.languageswitcher.model.LanguageState
import com.example.languageswitcher.model.Languages
import com.example.languageswitcher.repository.LanguageRepository
import com.example.languageswitcher.utils.LanguageManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LanguageViewModel(
    application: Application,
    private val languageRepository: LanguageRepository
) : AndroidViewModel(application) {

    private val _languageState = MutableStateFlow(LanguageState())
    val languageState: StateFlow<LanguageState> = _languageState.asStateFlow()

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet.asStateFlow()

    init {
        observeLanguageChanges()
    }

    private fun observeLanguageChanges() {
        viewModelScope.launch {
            languageRepository.selectedLanguageCode.collect { languageCode ->
                val language = Languages.getLanguageByCode(languageCode)
                val localizedStrings = loadLocalizedStrings(languageCode)

                _languageState.value = _languageState.value.copy(
                    currentLanguage = language,
                    localizedStrings = localizedStrings
                )
            }
        }
    }

    private fun loadLocalizedStrings(languageCode: String): Map<String, String> {
        return mapOf(
            "app_name" to LanguageManager.getLocalizedString(
                getApplication(),
                R.string.app_name,
                languageCode
            ),
            "select_language" to LanguageManager.getLocalizedString(
                getApplication(),
                R.string.select_language,
                languageCode
            ),
            "current_language" to LanguageManager.getLocalizedString(
                getApplication(),
                R.string.current_language,
                languageCode
            ),
            "welcome_message" to LanguageManager.getLocalizedString(
                getApplication(),
                R.string.welcome_message,
                languageCode
            ),
            "description" to LanguageManager.getLocalizedString(
                getApplication(),
                R.string.description,
                languageCode
            ),
            "change_language" to LanguageManager.getLocalizedString(
                getApplication(),
                R.string.change_language,
                languageCode
            )
        )
    }

    fun getLocalizedString(key: String): String {
        return _languageState.value.localizedStrings[key] ?: ""
    }

    fun showLanguageSelector() {
        _showBottomSheet.value = true
    }

    fun hideLanguageSelector() {
        _showBottomSheet.value = false
    }

    fun selectLanguage(language: Language) {
        viewModelScope.launch {
            val currentLanguage = _languageState.value.currentLanguage

            // Only change if language is different
            if (currentLanguage.code != language.code) {
                // Show loading state
                _languageState.value = _languageState.value.copy(isChangingLanguage = true)
                _showBottomSheet.value = false

                // Save language preference
                languageRepository.setLanguage(language.code)

                // Simulate loading time for better UX (optional)
                delay(800) // 800ms loading

                // Load new localized strings
                val newLocalizedStrings = loadLocalizedStrings(language.code)

                // Update state with new language and strings
                _languageState.value = _languageState.value.copy(
                    currentLanguage = language,
                    localizedStrings = newLocalizedStrings,
                    isChangingLanguage = false
                )
            } else {
                _showBottomSheet.value = false
            }
        }
    }
}