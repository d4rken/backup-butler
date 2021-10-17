package eu.darken.bb.common.preference

import android.content.SharedPreferences
import androidx.core.content.edit
import com.squareup.moshi.Moshi
import eu.darken.bb.common.debug.logging.Logging.Priority.*
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.moshi.fromJson
import eu.darken.bb.common.moshi.toJson
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.time.Instant
import java.util.*

class ObservablePreference<T : Any> constructor(
    private val preferences: SharedPreferences,
    private val key: String,
    private val reader: SharedPreferences.(key: String) -> T,
    private val writer: SharedPreferences.Editor.(key: String, value: T?) -> Unit,
) {
    private var internalValue: T?
        get() = reader(preferences, key)
        set(newValue) {
            preferences.edit { writer(key, newValue) }
            internalObservable.onNext(internalValue!!)
        }
    private val internalObservable = BehaviorSubject.createDefault<T>(internalValue!!)
    val observable: Observable<T> = internalObservable as Observable<T>

    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { changedPrefs, changedKey ->
            if (changedKey != key) return@OnSharedPreferenceChangeListener

            val newValue = reader(changedPrefs, changedKey)
            val currentvalue = internalObservable.value
            if (currentvalue != newValue) {
                internalObservable.onNext(newValue)
                log(VERBOSE) { "$changedPrefs:$changedKey changed to $newValue" }
            }
        }

    init {
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    val value: T
        get() = internalObservable.value!!

    fun update(update: (T) -> T?) {
        internalValue = update(internalObservable.value!!)
    }

    companion object {
        inline fun <reified T> moshiReader(
            moshi: Moshi,
            defaultValue: T,
        ): SharedPreferences.(key: String) -> T = { key ->
            getString(key, null)?.let { moshi.fromJson<T>(it) } ?: defaultValue
        }

        inline fun <reified T> moshiWriter(
            moshi: Moshi,
        ): SharedPreferences.Editor.(key: String, value: T) -> Unit = { key, value ->
            putString(key, value?.let { moshi.toJson(it) })
        }

        inline fun <reified T : Any> basicReader(defaultValue: T): SharedPreferences.(
            key: String
        ) -> T = { key ->
            when (T::class) {
                Instant::class -> all[key]?.let { Instant.ofEpochMilli(it as Long) as T } ?: defaultValue
                else -> (this.all[key] ?: defaultValue) as T
            }
        }

        inline fun <reified T : Any> basicWriter(): SharedPreferences.Editor.(
            key: String,
            value: T?
        ) -> Unit = { key, value ->
            when (value) {
                is Boolean -> putBoolean(key, value)
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                is Instant -> putLong(key, value.toEpochMilli())
                null -> remove(key)
                else -> throw NotImplementedError()
            }
        }
    }
}

inline fun <reified T : Any> SharedPreferences.createObservablePreference(
    key: String,
    defaultValue: T,
) = ObservablePreference(
    preferences = this,
    key = key,
    reader = ObservablePreference.basicReader(defaultValue),
    writer = ObservablePreference.basicWriter()
)

inline fun <reified T : Any> SharedPreferences.createObservablePreference(
    key: String,
    noinline reader: SharedPreferences.(key: String) -> T,
    noinline writer: SharedPreferences.Editor.(key: String, value: T?) -> Unit,
) = ObservablePreference(
    preferences = this,
    key = key,
    reader = reader,
    writer = writer
)