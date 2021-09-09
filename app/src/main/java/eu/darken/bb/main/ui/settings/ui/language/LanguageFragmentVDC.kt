package eu.darken.bb.main.ui.settings.ui.language

import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.common.vdc.SmartVDC
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
            State(
                current = languageEnforcer.lookup(languageEnforcer.currentLocale),
                languages = languages
            )
        }
        .toLiveData()
    val finishEvent = SingleLiveEvent<Any>()

    fun selectLanguage(language: LanguageEnforcer.Language, resources: Resources) {
        languageEnforcer.updateLanguage(language, resources)
        finishEvent.postValue(Any())
    }

    data class State(
        val languages: List<LanguageEnforcer.Language>,
        val current: LanguageEnforcer.Language
    )
}