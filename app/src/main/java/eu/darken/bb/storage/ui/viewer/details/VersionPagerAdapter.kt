package eu.darken.bb.storage.ui.viewer.details

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.storage.core.*
import eu.darken.bb.storage.ui.viewer.details.page.DetailPageFragment


class VersionPagerAdapter constructor(
        private val fragment: ContentDetailsFragment,
        private val storageId: Storage.Id,
        private val backupSpecId: BackupSpec.Id
) : FragmentStateAdapter(fragment), DataAdapter<Versioning.Version> {

    override val data: MutableList<Versioning.Version> = mutableListOf()

    override fun getItemCount(): Int = data.size

    override fun createFragment(position: Int): Fragment {
        val fragmentFactory = fragment.childFragmentManager.fragmentFactory
        val fragment = fragmentFactory.instantiate(this.javaClass.classLoader!!, DetailPageFragment::class.qualifiedName!!)
        val version = data[position]
        fragment.arguments = Bundle().apply {
            putStorageId(storageId)
            putBackupSpecId(backupSpecId)
            putBackupId(version.backupId)
        }
        return fragment
    }
}
