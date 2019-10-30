package eu.darken.bb.backup.core

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface GeneratorEditor {

    val editorData: Observable<out Data>

    fun load(config: Generator.Config): Completable

    fun save(): Single<out Generator.Config>

    fun isValid(): Observable<Boolean>

    fun release(): Completable

    interface Factory<T : GeneratorEditor> {
        fun create(generatorId: Generator.Id): T
    }

    interface Data {
        val generatorId: Generator.Id
        val label: String
        val isExistingGenerator: Boolean
    }
}