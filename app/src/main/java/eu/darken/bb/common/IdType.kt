package eu.darken.bb.common

interface IdType<T> : Comparable<T> {

    val value: Any
    val idString: String

}