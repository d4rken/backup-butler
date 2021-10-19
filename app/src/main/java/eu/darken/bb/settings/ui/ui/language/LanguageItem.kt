package eu.darken.bb.settings.ui.ui.language

import eu.darken.bb.main.core.LanguageEnforcer

data class LanguageItem(
    val language: LanguageEnforcer.Language,
    val isSelected: Boolean,
)