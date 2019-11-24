package eu.darken.bb.common.file.core.saf

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import io.kotlintest.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import testhelper.EmptyApp

@RunWith(AndroidJUnit4::class)
@Config(application = EmptyApp::class)
class SAFGatewayTest {

    @Test
    fun `test storage root detection`() {
        val safGateway = SAFGateway(
                mock { },
                mock { }
        )
        val nonRoot = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3Asafstor")
        safGateway.isStorageRoot(SAFPath.build(nonRoot)) shouldBe false
        safGateway.isStorageRoot(SAFPath.build(nonRoot, "crumb1")) shouldBe false

        val root = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3A")
        safGateway.isStorageRoot(SAFPath.build(root)) shouldBe true
        safGateway.isStorageRoot(SAFPath.build(root, "crumb1")) shouldBe false
    }
}