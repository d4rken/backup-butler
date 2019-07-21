package eu.darken.bb.backup.repos

data class RepoInfo(
        val label: String,
        val type: BackupRepo.Type,
        val reference: RepoReference
)