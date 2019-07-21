package eu.darken.bb.backup.repos

import android.os.Environment
import dagger.Reusable
import eu.darken.bb.backup.repos.local.LocalStorageRepoReference
import eu.darken.bb.common.file.JavaFile
import io.reactivex.Observable
import javax.inject.Inject

@Reusable
class RepoManager @Inject constructor() {

    fun infos(): Observable<Collection<RepoInfo>> {
        TODO()
    }

    @Synchronized
    fun references(): Observable<Collection<RepoReference>> {
        val refs = mutableListOf<RepoReference>()
        refs.add(LocalStorageRepoReference(JavaFile.build(Environment.getExternalStorageDirectory(), "1")))
        refs.add(LocalStorageRepoReference(JavaFile.build(Environment.getExternalStorageDirectory(), "2")))
        refs.add(LocalStorageRepoReference(JavaFile.build(Environment.getExternalStorageDirectory(), "3")))
        refs.add(LocalStorageRepoReference(JavaFile.build(Environment.getExternalStorageDirectory(), "4")))
        refs.add(LocalStorageRepoReference(JavaFile.build(Environment.getExternalStorageDirectory(), "5")))
        refs.add(LocalStorageRepoReference(JavaFile.build(Environment.getExternalStorageDirectory(), "6")))
        refs.add(LocalStorageRepoReference(JavaFile.build(Environment.getExternalStorageDirectory(), "7")))
        return Observable.just(refs.toList())
    }
}