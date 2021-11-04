package eu.darken.bb.backup.ui.generator.editor

import android.os.Parcelable
import eu.darken.bb.backup.core.Generator
import kotlinx.parcelize.Parcelize

@Parcelize
data class GeneratorEditorResult(
    val generatorId: Generator.Id,
) : Parcelable