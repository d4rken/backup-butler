package eu.darken.bb.processor.core.service

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import eu.darken.bb.backup.core.BackupModule

@InstallIn(ServiceComponent::class)
@Module(includes = [BackupModule::class])
class ProcessorServiceModule