package eu.darken.bb.common.debug


import dagger.Reusable
import eu.darken.bb.common.WebpageTool
import javax.inject.Inject


@Reusable
class ReportABug @Inject constructor(
    private val webpageTool: WebpageTool
) {
    fun reportABug() {
        webpageTool.open("https://bb.darken.eu/bugreport")
    }
}