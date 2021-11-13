package eu.darken.bb.settings.ui.ui.language

import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.smart.SmartVDC
import eu.darken.bb.main.core.LanguageEnforcer
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class LanguageFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    val languageEnforcer: LanguageEnforcer
) : SmartVDC() {

    val state = Observable.fromCallable { languageEnforcer.supportedLocales }
        .subscribeOn(Schedulers.computation())
        .map { locales ->
            locales
                .map { languageEnforcer.lookup(it) }
                .filter { it.isTranslated }
        }
        .map { languages ->
            val current = languageEnforcer.lookup(languageEnforcer.currentLocale)
            State(languages = languages.map { LanguageItem(it, it == current) })
        }
        .asLiveData()
    val finishEvent = SingleLiveEvent<Any>()

    fun selectLanguage(language: LanguageEnforcer.Language, resources: Resources) {
        languageEnforcer.updateLanguage(language, resources)
        finishEvent.postValue(Any())
    }

    data class State(
        val languages: List<LanguageItem>,
    )
}