package testhelper.preference

import eu.darken.bb.common.preference.ObservablePreference
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

fun <T : Any> mockObservablePreference(
    defaultValue: T
): ObservablePreference<T> {

    val instance = mockk<ObservablePreference<T>>()
    val subject = BehaviorSubject.createDefault(defaultValue)
    every { instance.observable } answers { subject as Observable<T> }
    every { instance.value } answers { subject.value }
    every { instance.update(any()) } answers {
        val updateCall = arg<(T) -> T>(0)
        subject.onNext(updateCall(subject.value!!))
    }

    return instance
}