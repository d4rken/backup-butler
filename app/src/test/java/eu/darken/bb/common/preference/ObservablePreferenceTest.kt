package eu.darken.bb.common.preference

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelper.BaseTest
import testhelper.preference.MockSharedPreferences
import testhelper.toFormattedJson

class ObservablePreferenceTest : BaseTest() {

    private val mockPreferences = MockSharedPreferences()

    @Test
    fun `reading and writing strings`() {
        mockPreferences.createObservablePreference(
            key = "testKey",
            defaultValue = "default"
        ).apply {
            value shouldBe "default"
            observable.blockingFirst() shouldBe "default"
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true

            update {
                it shouldBe "default"
                "newvalue"
            }

            value shouldBe "newvalue"
            observable.blockingFirst() shouldBe "newvalue"
            mockPreferences.dataMapPeek.values.first() shouldBe "newvalue"

            update {
                it shouldBe "newvalue"
                null
            }
            value shouldBe "default"
            observable.blockingFirst() shouldBe "default"
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true
        }
    }

    @Test
    fun `reading and writing boolean`() {
        mockPreferences.createObservablePreference(
            key = "testKey",
            defaultValue = true
        ).apply {
            value shouldBe true
            observable.blockingFirst() shouldBe true
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true

            update {
                it shouldBe true
                false
            }

            value shouldBe false
            observable.blockingFirst() shouldBe false
            mockPreferences.dataMapPeek.values.first() shouldBe false

            update {
                it shouldBe false
                null
            }
            value shouldBe true
            observable.blockingFirst() shouldBe true
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true
        }
    }

    @Test
    fun `reading and writing long`() {
        mockPreferences.createObservablePreference(
            key = "testKey",
            defaultValue = 9000L
        ).apply {
            value shouldBe 9000L
            observable.blockingFirst() shouldBe 9000L
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true

            update {
                it shouldBe 9000L
                9001L
            }

            value shouldBe 9001L
            observable.blockingFirst() shouldBe 9001L
            mockPreferences.dataMapPeek.values.first() shouldBe 9001L

            update {
                it shouldBe 9001L
                null
            }
            value shouldBe 9000L
            observable.blockingFirst() shouldBe 9000L
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true
        }
    }

    @Test
    fun `reading and writing integer`() {
        mockPreferences.createObservablePreference<Long>(
            key = "testKey",
            defaultValue = 123
        ).apply {
            value shouldBe 123
            observable.blockingFirst() shouldBe 123
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true

            update {
                it shouldBe 123
                44
            }

            value shouldBe 44
            observable.blockingFirst() shouldBe 44
            mockPreferences.dataMapPeek.values.first() shouldBe 44

            update {
                it shouldBe 44
                null
            }
            value shouldBe 123
            observable.blockingFirst() shouldBe 123
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true
        }
    }

    @Test
    fun `reading and writing float`() {
        mockPreferences.createObservablePreference<Float>(
            key = "testKey",
            defaultValue = 3.6f
        ).apply {
            value shouldBe 3.6f
            observable.blockingFirst() shouldBe 3.6f
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true

            update {
                it shouldBe 3.6f
                15000f
            }

            value shouldBe 15000f
            observable.blockingFirst() shouldBe 15000f
            mockPreferences.dataMapPeek.values.first() shouldBe 15000f

            update {
                it shouldBe 15000f
                null
            }
            value shouldBe 3.6f
            observable.blockingFirst() shouldBe 3.6f
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true
        }
    }

    @Keep
    @JsonClass(generateAdapter = true)
    data class SerializationTestData(
        val string: String = "",
        val boolean: Boolean = true,
        val float: Float = 1.0f,
        val int: Int = 1,
        val long: Long = 1L
    )

    @Test
    fun `reading and writing with serialization`() {
        val testData1 = SerializationTestData(string = "teststring")
        val testData2 = SerializationTestData(string = "update")
        val moshi = Moshi.Builder().build()
        ObservablePreference(
            preferences = mockPreferences,
            key = "testKey",
            reader = ObservablePreference.moshiReader(moshi, testData1),
            writer = ObservablePreference.moshiWriter(moshi)
        ).apply {
            value shouldBe testData1
            observable.blockingFirst() shouldBe testData1
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true

            update {
                it shouldBe testData1
                it!!.copy(string = "update")
            }

            value shouldBe testData2
            observable.blockingFirst() shouldBe testData2
            (mockPreferences.dataMapPeek.values.first() as String).toFormattedJson() shouldBe """
                {
                    "string":"update",
                    "boolean":true,
                    "float":1.0,
                    "int":1,
                    "long":1
                }
            """.toFormattedJson()

            update {
                it shouldBe testData2
                null
            }
            value shouldBe testData1
            observable.blockingFirst() shouldBe testData1
            mockPreferences.dataMapPeek.values.isEmpty() shouldBe true
        }
    }
}