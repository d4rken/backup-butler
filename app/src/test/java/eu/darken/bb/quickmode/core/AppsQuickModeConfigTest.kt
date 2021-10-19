package eu.darken.bb.quickmode.core

import eu.darken.bb.AppModule
import eu.darken.bb.task.core.Task
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelper.toFormattedJson

class AppsQuickModeConfigTest {

    @Test
    fun `test serialization`() {
        val original = AppsQuickModeConfig(
            taskId = Task.Id(),
        )

        original.type shouldBe QuickMode.Type.APPS

        val expectedOutput = """{
            "taskId": "${original.taskId!!.idString}",
            "type": "APPS"
        }""".toFormattedJson()

        val adapterDirect = AppModule().moshi().adapter(AppsQuickModeConfig::class.java)
        val jsonDirect = adapterDirect.toJson(original)
        jsonDirect.toFormattedJson() shouldBe expectedOutput
        adapterDirect.fromJson(jsonDirect) shouldBe original

        val adapterPoly = AppModule().moshi().adapter(QuickMode.Config::class.java)
        val jsonPoly = adapterPoly.toJson(original)
        jsonPoly.toFormattedJson() shouldBe expectedOutput
        adapterPoly.fromJson(jsonPoly) shouldBe original
    }

}