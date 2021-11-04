package eu.darken.bb.backup.ui.generator.picker

import android.os.Parcelable
import eu.darken.bb.backup.core.Generator
import kotlinx.parcelize.Parcelize

@Parcelize
data class GeneratorPickerResult(
    val generatorId: Generator.Id,
) : Parcelable