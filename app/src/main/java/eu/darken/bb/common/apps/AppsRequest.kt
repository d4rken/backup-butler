package eu.darken.bb.common.apps


import java.util.*

data class AppsRequest(val acceptableAge: Age, val flags: Int) {
    companion object {
        val REFRESH = AppsRequest(Age.FRESH, 0)
        val NON_STALE = AppsRequest(Age.RECENTLY, 0)
        val CACHED = AppsRequest(Age.ANY, 0)
    }

    enum class Age(internal val age: Long) {
        ANY(-1), FRESH(0), RECENTLY(60 * 1000)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as AppsRequest?
        return flags == that!!.flags
    }

    override fun hashCode(): Int {
        return Objects.hash(flags)
    }

    override fun toString(): String {
        return "AppsRequest(acceptableAge=$acceptableAge, flags=$flags)"
    }
}
