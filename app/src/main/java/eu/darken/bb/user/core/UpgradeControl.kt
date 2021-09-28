package eu.darken.bb.user.core

import com.jakewharton.rx3.replayingShare
import eu.darken.bb.common.debug.logging.logTag
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpgradeControl @Inject constructor(

) {
    companion object {
        val TAG = logTag("UpgradeControl")
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