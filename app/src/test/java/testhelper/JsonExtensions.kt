package testhelper

import okio.ByteString.Companion.encode
import okio.buffer
import okio.sink
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

fun String.toFormattedJson() = toJSONObject().toFormattedJson()

private fun JSONObject.toFormattedJson() = toString(4)

private fun String.toJSONObject() = JSONObject(this)

fun String.toJsonMap() = toJSONObject().entries().toMap()

fun JSONArray.toJSONObjects(): List<JSONObject> = (0 until length()).map { get(it) as JSONObject }

fun JSONObject.entries(): Sequence<Pair<String, Any?>> = this.keys().asSequence().map { key ->
    key as String to get(key)
}

fun Map<String, Any>.toFormattedJson() = toJSONObject().toFormattedJson()

fun Map<String, Any>.toJSONObject() = JSONObject(this)

fun String.writeToFile(file: File) = encode().let { text ->
    require(!file.exists())
    file.parentFile?.mkdirs()
    file.createNewFile()
    file.sink().buffer().use { it.write(text) }
}