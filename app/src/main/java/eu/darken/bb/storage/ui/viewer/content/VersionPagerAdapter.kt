package eu.darken.bb.storage.ui.viewer.content

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.putBackupId
import eu.darken.bb.backup.core.putBackupSpecId
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.Versioning
import eu.darken.bb.storage.core.putStorageId
import eu.darken.bb.storage.ui.viewer.content.page.ContentPageFragment


class VersionPagerAdapter constructor(
        private val fragment: ItemContentsFragment,
        private val storageId: Storage.Id,
        private val backupSpecId: BackupSpec.Id
) : FragmentStateAdapter(fragment), DataAdapter<Versioning.Version> {

    override val data: MutableList<Versioning.Version> = mutableListOf()

    override fun getItemCount(): Int = data.size

    override fun createFragment(position: Int): Fragment {
        val fragmentFactory = fragment.childFragmentManager.fragmentFactory
        val fragment = fragmentFactory.instantiate(this.javaClass.classLoader!!, ContentPageFragment::class.qualifiedName!!)
        val version = data[position]
        fragment.arguments = Bundle().apply {
            putStorageId(storageId)
            putBackupSpecId(backupSpecId)
            putBackupId(version.backupId)
        }
        return fragment
    }
}
