package eu.darken.bb.common


fun <K, V> MutableMap<K, V>.update(key: K, update: (V?) -> V?) {
    val existing = this[key]
    val updated = update(existing)
    if (updated == null) {
        remove(key)
    } else {
        put(key, updated)
    }
}