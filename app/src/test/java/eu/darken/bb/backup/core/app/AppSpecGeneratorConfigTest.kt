package eu.darken.bb.backup.core.app

import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.file.RawPath
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class AppSpecGeneratorConfigTest {

    val original = AppSpecGenerator.Config(
            generatorId = Generator.Id(),
            label = "AppSpecLabel",
            autoInclude = true,
            includeUserApps = true,
            includeSystemApps = false,
            packagesIncluded = listOf("included.pkg"),
            packagesExcluded = listOf("excluded.pkg"),
            backupApk = true,
            backupData = true,
            backupCache = false,
            extraPaths = mapOf("test" to listOf(RawPath.build("rawpath")))
    )

    @Test
    fun `test serialization`() {
        val moshi = AppModule().moshi()

        val expectedJson = "{" +
                "\"generatorId\":\"${original.generatorId.idString}\"," +
                "\"label\":\"AppSpecLabel\"," +
                "\"autoInclude\":true," +
                "\"includeUserApps\":true," +
                "\"includeSystemApps\":false," +
                "\"packagesIncluded\":[\"included.pkg\"]," +
                "\"packagesExcluded\":[\"excluded.pkg\"]," +
                "\"backupApk\":true," +
                "\"backupData\":true," +
                "\"backupCache\":false," +
                "\"extraPaths\":{\"test\":[{\"path\":\"rawpath\",\"pathType\":\"RAW\"}]}," +
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
        original.generatorType shouldBe Backup.Type.APP
        original.generatorType = Backup.Type.FILES
        original.generatorType shouldBe Backup.Type.APP
    }

}