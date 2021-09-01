package eu.darken.bb.common.previews

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Priority
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import dagger.Lazy
import eu.darken.bb.App
import eu.darken.bb.common.files.core.GatewaySwitch
import eu.darken.bb.common.pkgs.pkgops.PkgOps
import eu.darken.bb.common.previews.decoder.*
import eu.darken.bb.common.previews.model.*
import javax.inject.Inject

@GlideModule
class GlideConfigModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDefaultRequestOptions(
            RequestOptions
                .diskCacheStrategyOf(DiskCacheStrategy.NONE)
                .priority(Priority.LOW)
        )
    }

    @Inject lateinit var pkgOpsLazy: Lazy<PkgOps>
    @Inject lateinit var pkgUriIconLoaderFactory: PkgUriIconLoader.Factory
    @Inject lateinit var appInfoIconLoaderFactory: AppInfoIconLoader.Factory
    @Inject lateinit var smartFileModelLoaderFactory: SmartFileModelLoader.Factory
    @Inject lateinit var gatewaySwitch: GatewaySwitch

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        (context.applicationContext as App).appComponent.inject(this)

        registry.append(PkgUriPreviewRequest::class.java, AppIconData::class.java, pkgUriIconLoaderFactory)
        registry.append(AppPreviewRequest::class.java, AppIconData::class.java, appInfoIconLoaderFactory)
        registry.append(FilePreviewRequest::class.java, FileData::class.java, smartFileModelLoaderFactory)

        registry.append(AppIconData::class.java, Bitmap::class.java, AppIconDecoder(context, glide, pkgOpsLazy))
        registry.append(FileData::class.java, Bitmap::class.java, ImageDecoder(context, glide))
        registry.append(FileData::class.java, Bitmap::class.java, ApkDecoder(context, glide, pkgOpsLazy, gatewaySwitch))
        registry.append(FileData::class.java, Bitmap::class.java, VideoDecoder(context, glide))
        registry.append(FileData::class.java, Bitmap::class.java, MusicDecoder(context, glide))
        registry.append(FileData::class.java, Bitmap::class.java, FallbackDecoder(context, glide))
    }

    override fun isManifestParsingEnabled(): Boolean = false
}
