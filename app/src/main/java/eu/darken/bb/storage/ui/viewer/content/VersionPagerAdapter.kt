package eu.darken.bb.storage.ui.viewer.content

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.lists.DataAdapter
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.ui.viewer.content.page.ContentPageFragment
import eu.darken.bb.storage.ui.viewer.content.page.ContentPageFragmentArgs


class VersionPagerAdapter constructor(
    private val fragment: StorageContentFragment,
    private val storageId: Storage.Id,
    private val backupSpecId: BackupSpec.Id
) : FragmentStateAdapter(fragment), DataAdapter<Backup.MetaData> {

    override val data: MutableList<Backup.MetaData> = mutableListOf()

    override fun getItemCount(): Int = data.size

    override fun createFragment(position: Int): Fragment {
        val fragmentFactory = fragment.childFragmentManager.fragmentFactory
        val fragment =
            fragmentFactory.instantiate(this.javaClass.classLoader!!, ContentPageFragment::class.qualifiedName!!)
        val version = data[position]

        fragment.arguments = ContentPageFragmentArgs(
            storageId,
            backupSpecId,
            version.backupId,
        ).toBundle()
        return fragment
    }
}
