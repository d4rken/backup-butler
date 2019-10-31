package eu.darken.bb.common.apps


data class AppsRequest(val acceptableAge: Age, val flags: Int) {

    enum class Age constructor(internal val value: Long) {
        ANY(-1), FRESH(0), RECENTLY(60 * 1000)
    }

    override fun toString(): String = "AppsRequest(acceptableAge=$acceptableAge, flags=$flags)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppsRequest
        if (flags != other.flags) return false
        return true
    }

    override fun hashCode(): Int {
        return flags
    }

    companion object {
        val REFRESH = AppsRequest(Age.FRESH, 0)
        val NON_STALE = AppsRequest(Age.RECENTLY, 0)
        val CACHED = AppsRequest(Age.ANY, 0)
    }
}
