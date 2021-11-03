package eu.darken.bb.common.pkgs.picker.ui

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import com.jakewharton.rx3.replayingShare
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.pkgs.NormalPkg
import eu.darken.bb.common.pkgs.picker.core.PickedPkg
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.vdc.SmartVDC
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject

@HiltViewModel
class PkgPickerFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val pkgOps: PkgOps,
) : SmartVDC() {

    private val navArgs by handle.navArgs<PkgPickerFragmentArgs>()
    private val options = navArgs.options

    private val pkgData = Observable
        .create<List<NormalPkg>> { emitter ->
            val pkgs = pkgOps.listPkgs()
                .filterIsInstance<NormalPkg>()
                .filter { options.allowSystemApps || !it.isSystemApp }
            emitter.onNext(pkgs)
        }
        .subscribeOn(Schedulers.computation())
        .replayingShare()

    private val selectedItems = BehaviorSubject.createDefault(emptyList<String>())

    val navEvents = SingleLiveEvent<NavDirections>()

    val state = Observables.combineLatest(pkgData, selectedItems)
        .observeOn(Schedulers.computation())
        .map { (pkgs, selected) ->
            PkgsState(
                items = pkgs.map { pkg ->
                    PkgPickerAdapter.Item(
                        pkg = pkg,
                        label = pkg.getLabel(pkgOps) ?: pkg.packageName,
                        isSelected = selected.any { it == pkg.packageName }
                    )
                },
                selected = selected,
            )
        }
        .asLiveData()

    val finishEvent = SingleLiveEvent<PkgPickerResult?>()

    fun selectPkg(item: PkgPickerAdapter.Item) {
        log(TAG) { "selectPkg(item=$item)" }
        selectedItems.value!!
            .let {
                val newSelection = when {
                    item.isSelected -> it.minus(item.pkg.packageName)
                    else -> it.plus(item.pkg.packageName)
                }
                if (newSelection.size > options.selectionLimit) {
                    newSelection.subList(1, options.selectionLimit + 1)
                } else {
                    newSelection
                }
            }
            .run { selectedItems.onNext(this) }
    }

    fun done() {
        PkgPickerResult(
            options = options,
            error = null,
            selection = selectedItems.value!!.map { PickedPkg(it) }.toSet(),
            payload = Bundle(),
        ).run { finishEvent.postValue(this) }
    }

    fun selectAll() {
        selectedItems.onNext(pkgData.blockingFirst().map { it.packageName })
    }

    fun unselectAll() {
        selectedItems.onNext(emptyList())
    }

    data class PkgsState(
        val items: List<PkgPickerAdapter.Item> = emptyList(),
        val selected: List<String> = emptyList()
    )

    companion object {
        private val TAG = logTag("Pkg", "Picker", "VDC")
    }
}