package eu.darken.bb.backup.core.file

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.moshi.Moshi
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.HotData
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class FileSpecGeneratorEditor @AssistedInject constructor(
        @Assisted private val generatorId: Generator.Id,
        moshi: Moshi
) : Generator.Editor {

    private val configPub = HotData(FileBackupGenerator.Config(generatorId = generatorId))
    override val config: Observable<out Generator.Config> = configPub.data

    override var existingConfig: Boolean = false

    override fun isValid(): Observable<Boolean> = Observable.just(false)

    override fun load(config: Generator.Config): Completable {
        TODO("not implemented")
    }

    override fun save(): Single<out Generator.Config> {
        TODO("not implemented")
    }


    @AssistedInject.Factory
    interface Factory : Generator.Editor.Factory<FileSpecGeneratorEditor>

}