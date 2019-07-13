package eu.darken.bb

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import eu.darken.bb.backup.backups.Backup


@Module
class AppModule {

    @Provides
    @AppComponent.Scope
    fun moshi(): Moshi {
        val moshi = Moshi.Builder()
                .add(Backup.Config.MOSHI_FACTORY)
                .add(KotlinJsonAdapterFactory())
                .build()
        return moshi
    }

}
