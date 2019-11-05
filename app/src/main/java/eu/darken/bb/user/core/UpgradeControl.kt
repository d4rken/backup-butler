package eu.darken.bb.user.core

import com.jakewharton.rx.replayingShare
import eu.darken.bb.App
import eu.darken.bb.common.dagger.PerApp
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@PerApp
class UpgradeControl @Inject constructor(

) {
    companion object {
        val TAG = App.logTag("UpgradeControl")
    }

    private lateinit var cascChecks: CompositeDisposable
    private val upgradeDataPub: Subject<UpgradeData> = BehaviorSubject.createDefault(
            UpgradeData(
                    state = UpgradeData.State.PRO,
                    features = listOf(UpgradeData.Feature.BACKUP)
            )
    )

    val upgradeData: Observable<out UpgradeData> = upgradeDataPub
            .subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
            .doOnSubscribe {
                Timber.tag(TAG).d("upgradeData.doOnSubscribe")
                cascChecks = CompositeDisposable()

            }
            .doOnNext { Timber.tag(TAG).d("upgradeData.onNext()   : %s", it) }
            .doFinally {
                Timber.tag(TAG).d("upgradeData.doFinally")
                cascChecks.dispose()
            }
            .throttleLast(250, TimeUnit.MILLISECONDS)
            .doOnNext { Timber.tag(TAG).i("upgradeData.onNext()[t]: %s", it) }
            .replayingShare()
            .doOnSubscribe { Timber.tag(TAG).v("Client sub: %s", Thread.currentThread()) }
            .doOnDispose { Timber.tag(TAG).v("Client disp") }

}