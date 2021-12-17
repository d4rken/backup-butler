package eu.darken.bb.backup.core.files

import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.RawPath
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelper.toFormattedJson

class FilesSpecGeneratorConfigTest {

    @Test
    fun `test serialization`() {
        val original = FilesSpecGenerator.Config(
            generatorId = Generator.Id(),
            label = "FilesSpecLabel",
            path = RawPath.build("testcrumb"),
            isSingleUse = false
        )

        val moshi = AppModule().moshi()
        val adapterPath = moshi.adapter(APath::class.java)
        val jsonPath = adapterPath.toJson(original.path)

        val expectedJson = """
            {
                "generatorId":"${original.generatorId.idString}",
                "label":"FilesSpecLabel",
                "path":$jsonPath,
                "generatorType":"FILES",
                "isSingleUse": false
            }
        """.toFormattedJson()

        val adapterPoly = moshi.adapter(Generator.Config::class.java)
        val jsonPoly = adapterPoly.toJson(original)
        jsonPoly.toFormattedJson() shouldBe expectedJson
        adapterPoly.fromJson(jsonPoly) shouldBe original

        val adapterDirect = moshi.adapter(FilesSpecGenerator.Config::class.java)
        val jsonDirect = adapterDirect.toJson(original)
        jsonDirect.toFormattedJson() shouldBe expectedJson
        adapterDirect.fromJson(jsonDirect) shouldBe original
    }

    @Test
    fun `test fixed type`() {
        val original = FilesSpecGenerator.Config(
            generatorId = Generator.Id(),
            label = "TestLabel",
            path = RawPath.build("testcrumb"),
            isSingleUse = false
        )
        original.generatorType shouldBe Backup.Type.FILES
        original.generatorType = Backup.Type.APP
        original.generatorType shouldBe Backup.Type.FILES
    }

}