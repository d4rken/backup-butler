package eu.darken.bb.repos.ui.list

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import eu.darken.bb.R
import eu.darken.bb.common.BaseVH
import eu.darken.bb.common.BindableVH
import eu.darken.bb.common.getColorForAttr
import eu.darken.bb.common.tryLocalizedErrorMessage
import eu.darken.bb.repos.core.RepoStatus
import javax.inject.Inject

class RepoListAdapter @Inject constructor() : RecyclerView.Adapter<RepoListAdapter.BackupVH>() {

    var data = listOf<RepoStatus>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackupVH = BackupVH(parent)

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: BackupVH, position: Int) = holder.bind(data[position])


    class BackupVH(parent: ViewGroup) : BaseVH(R.layout.repolist_adapter_line, parent), BindableVH<RepoStatus> {
        @BindView(R.id.type_label) lateinit var typeLabel: TextView
        @BindView(R.id.type_icon) lateinit var typeIcon: ImageView
        @BindView(R.id.path) lateinit var path: TextView
        @BindView(R.id.repo_status) lateinit var repoStatus: TextView


        init {
            ButterKnife.bind(this, itemView)
        }

        override fun bind(item: RepoStatus) {
            if (item.info != null) {
                typeLabel.setText(item.ref.repoType.typeLabel)
                typeIcon.setImageResource(item.ref.repoType.typeIcon)
                path.text = item.info.label
                repoStatus.setTextColor(context.getColorForAttr(android.R.attr.textColorSecondary))
                repoStatus.text = "Count: TODO; Size: TODO"
            } else if (item.error != null) {
                typeLabel.setText(item.ref.repoType.typeLabel)
                typeIcon.setImageResource(item.ref.repoType.typeIcon)
                path.text = "?"
                repoStatus.setTextColor(getColor(R.color.colorError))
                repoStatus.text = item.error.tryLocalizedErrorMessage(context)
            }

        }

    }
}