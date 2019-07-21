package eu.darken.bb.main.ui.repos

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.backup.repos.RepoInfo
import eu.darken.bb.common.BaseVH
import eu.darken.bb.common.BindableVH
import javax.inject.Inject

class RepoListAdapter @Inject constructor() : RecyclerView.Adapter<RepoListAdapter.BackupVH>() {

    var data = listOf<RepoInfo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackupVH = BackupVH(parent)

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: BackupVH, position: Int) = holder.bind(data[position])


    class BackupVH(parent: ViewGroup) : BaseVH(R.layout.repolist_adapter_line, parent), BindableVH<RepoInfo> {
        @BindView(R.id.type_label) lateinit var typeLabel: TextView
        @BindView(R.id.type_icon) lateinit var typeIcon: ImageView
        @BindView(R.id.path) lateinit var path: TextView
        @BindView(R.id.repo_stats) lateinit var repoStats: TextView
        @BindView(R.id.repo_status) lateinit var repoStatus: TextView


        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: RepoInfo) {
            typeLabel.setText(item.type.typeLabel)
            typeIcon.setImageResource(item.type.typeIcon)
            path.text = item.label
        }

    }
}