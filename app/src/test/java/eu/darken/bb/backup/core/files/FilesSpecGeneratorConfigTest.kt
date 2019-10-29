package eu.darken.bb.backup.core.files

import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.file.APath
import eu.darken.bb.common.file.RawPath
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class FilesSpecGeneratorConfigTest {

    @Test
    fun `test serialization`() {
        val original = FilesSpecGenerator.Config(
                generatorId = Generator.Id(),
                label = "FilesSpecLabel",
                path = RawPath.build("testcrumb")
        )

        val moshi = AppModule().moshi()
        val adapterPath = moshi.adapter(APath::class.java)
        val jsonPath = adapterPath.toJson(original.path)

        val expectedJson = "{" +
                "\"generatorId\":\"${original.generatorId.idString}\"," +
                "\"label\":\"FilesSpecLabel\"," +
                "\"path\":$jsonPath," +
                "\"generatorType\":\"FILES\"" +
                "}"

        val adapterPoly = moshi.adapter(Generator.Config::class.java)
        val jsonPoly = adapterPoly.toJson(original)
        jsonPoly shouldBe expectedJson
        adapterPoly.fromJson(jsonPoly) shouldBe original

        val adapterDirect = moshi.adapter(FilesSpecGenerator.Config::class.java)
        val jsonDirect = adapterDirect.toJson(original)
        jsonDirect shouldBe expectedJson
        adapterDirect.fromJson(jsonDirect) shouldBe original
    }

    @Test
    fun `test fixed type`() {
        val original = FilesSpecGenerator.Config(
                generatorId = Generator.Id(),
                label = "TestLabel",
                path = RawPath.build("testcrumb")
        )
        original.generatorType shouldBe Backup.Type.FILES
        original.generatorType = Backup.Type.APP
        original.generatorType shouldBe Backup.Type.FILES
    }

}