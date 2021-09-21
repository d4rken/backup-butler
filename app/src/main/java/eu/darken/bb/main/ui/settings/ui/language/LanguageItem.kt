package eu.darken.bb.main.ui.settings.ui.language

import eu.darken.bb.main.core.LanguageEnforcer

data class LanguageItem(
    val language: LanguageEnforcer.Language,
    val isSelected: Boolean,
)