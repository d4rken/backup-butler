package eu.darken.bb.common.file

import java.io.IOException

private fun constructMessage(file: APath, other: APath?, reason: String?): String {
    val sb = StringBuilder(file.toString())
    if (other != null) {
        sb.append(" -> $other")
    }
    if (reason != null) {
        sb.append(": $reason")
    }
    return sb.toString()
}

/**
 * A base exception class for file system exceptions.
 * @property file the file on which the failed operation was performed.
 * @property other the second file involved in the operation, if any (for example, the target of a copy or move)
 * @property reason the description of the error
 */
open class APathException(
        val file: APath,
        val other: APath? = null,
        val reason: String? = null
) : IOException(constructMessage(file, other, reason))

/**
 * An exception class which is used when some file to create or copy to already exists.
 */
class FileAlreadyExistsException(
        file: APath,
        other: APath? = null,
        reason: String? = null
) : APathException(file, other, reason)

/**
 * An exception class which is used when we have not enough access for some operation.
 */
class AccessDeniedException(
        file: APath,
        other: APath? = null,
        reason: String? = null
) : APathException(file, other, reason)

/**
 * An exception class which is used when file to copy does not exist.
 */
class NoSuchFileException(
        file: APath,
        other: APath? = null,
        reason: String? = null
) : APathException(file, other, reason)

