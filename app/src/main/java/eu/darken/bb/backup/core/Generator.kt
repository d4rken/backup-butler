package eu.darken.bb.backup.core

import android.content.Context
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.R
import eu.darken.bb.backup.core.app.AppSpecGenerator
import eu.darken.bb.backup.core.files.legacy.LegacyFilesSpecGenerator
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.android.parcel.Parcelize
import java.util.*

interface Generator {

    fun generate(config: Config): Collection<BackupSpec>

    interface Editor {

        val config: Observable<out Config>

        val existingConfig: Boolean

        fun isValid(): Observable<Boolean>

        fun save(): Single<out Config>

        fun load(config: Config): Completable

        interface Factory<T : Editor> {
            fun create(generatorId: Id): T
        }
    }

    @Keep
    enum class Type constructor(
            @DrawableRes val iconRes: Int,
            @StringRes val labelRes: Int,
            @StringRes val descriptionRes: Int
    ) {
        APP(R.drawable.ic_apps, R.string.generator_type_app_label, R.string.generator_type_app_desc),
        FILE_LEGACY(R.drawable.ic_folder, R.string.generator_type_file_legacy_label, R.string.generator_type_file_legacy_desc),
        FILE_SAF(R.drawable.ic_folder, R.string.generator_type_file_saf_label, R.string.generator_type_file_saf_desc),
        FILE_ROOT(R.drawable.ic_folder, R.string.generator_type_file_root_label, R.string.generator_type_file_root_desc);
    }

    interface Config {
        val generatorId: Id
        val generatorType: Type
        val label: String

        fun getDescription(context: Context): String

        companion object {
            val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<Config> = PolymorphicJsonAdapterFactory.of(Config::class.java, "generatorType")
                    .withSubtype(AppSpecGenerator.Config::class.java, Type.APP.name)
                    .withSubtype(LegacyFilesSpecGenerator.Config::class.java, Type.FILE_LEGACY.name)
        }
    }

    @Parcelize
    data class Id(val id: UUID = UUID.randomUUID()) : Parcelable {
        override fun toString(): String = "GeneratorId($id)"
    }
}