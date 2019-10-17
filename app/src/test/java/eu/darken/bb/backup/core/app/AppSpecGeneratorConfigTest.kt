package eu.darken.bb.backup.core.app

import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Generator
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class AppSpecGeneratorConfigTest {

    @Test
    fun `test serialization`() {
        val original = AppSpecGenerator.Config(
                generatorId = Generator.Id(),
                label = "AppSpecLabel"
        )

        val moshi = AppModule().moshi()

        val expectedJson = "{" +
                "\"generatorId\":\"${original.generatorId.idString}\"," +
                "\"label\":\"AppSpecLabel\"," +
                "\"autoIncludeApps\":false," +
                "\"includeSystemApps\":false," +
                "\"packagesIncluded\":[]," +
                "\"packagesExcluded\":[]," +
                "\"backupApk\":false," +
                "\"backupData\":false," +
                "\"extraPaths\":{}," +
                "\"generatorType\":\"APP\"" +
                "}"

        val adapterPoly = moshi.adapter(Generator.Config::class.java)
        val jsonPoly = adapterPoly.toJson(original)
        jsonPoly shouldBe expectedJson
        adapterPoly.fromJson(jsonPoly) shouldBe original

        val adapterDirect = moshi.adapter(AppSpecGenerator.Config::class.java)
        val jsonDirect = adapterDirect.toJson(original)
        jsonDirect shouldBe expectedJson
        adapterDirect.fromJson(jsonDirect) shouldBe original
    }

    @Test
    fun `test fixed type`() {
        val original = AppSpecGenerator.Config(
                generatorId = Generator.Id()
        )
        original.generatorType shouldBe Backup.Type.APP
        original.generatorType = Backup.Type.FILES
        original.generatorType shouldBe Backup.Type.APP
    }

}