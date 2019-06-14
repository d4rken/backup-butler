package eu.darken.bb.upgrades

data class UpgradeData(
        val state: State,
        val features: Collection<Feature>,
        val validity: Long = -1L
) {

    enum class Feature {
        BACKUP
    }

    enum class State {
        BASIC, PRO
    }
}