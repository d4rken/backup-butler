package eu.darken.bb.backup.core.app

import eu.darken.bb.AppModule
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.files.core.RawPath
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelper.toFormattedJson

class AppSpecGeneratorConfigTest {

    val original = AppSpecGenerator.Config(
        generatorId = Generator.Id(),
        label = "AppSpecLabel",
        autoInclude = true,
        includeUserApps = true,
        includeSystemApps = false,
        packagesIncluded = setOf("included.pkg"),
        packagesExcluded = setOf("excluded.pkg"),
        backupApk = true,
        backupData = true,
        backupCache = false,
        extraPaths = mapOf("test" to setOf(RawPath.build("rawpath")))
    )

    @Test
    fun `test serialization`() {
        val moshi = AppModule().moshi()

        val expectedJson = """
            {
                "generatorId": "${original.generatorId.idString}",
                "label": "AppSpecLabel",
                "autoInclude": true,
                "includeUserApps": true,
                "includeSystemApps": false,
                "packagesIncluded": [
                    "included.pkg"
                ],
                "packagesExcluded": [
                    "excluded.pkg"
                ],
                "backupApk": true,
                "backupData": true,
                "backupCache": false,
                "extraPaths": {
                    "test": [
                        {
                            "path": "rawpath",
                            "pathType": "RAW"
                        }
                    ]
                },
                "generatorType": "APP"
            }
        """.toFormattedJson()

        val adapterPoly = moshi.adapter(Generator.Config::class.java)
        val jsonPoly = adapterPoly.toJson(original)
        jsonPoly.toFormattedJson() shouldBe expectedJson
        adapterPoly.fromJson(jsonPoly) shouldBe original

        val adapterDirect = moshi.adapter(AppSpecGenerator.Config::class.java)
        val jsonDirect = adapterDirect.toJson(original)
        jsonDirect.toFormattedJson() shouldBe expectedJson
        adapterDirect.fromJson(jsonDirect) shouldBe original
    }

    @Test
    fun `test fixed type`() {
        original.generatorType shouldBe Backup.Type.APP
        original.generatorType = Backup.Type.FILES
        original.generatorType shouldBe Backup.Type.APP
    }

}