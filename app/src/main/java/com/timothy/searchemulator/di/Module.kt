package com.timothy.searchemulator.di

import com.timothy.searchemulator.ui.emulator.MovementRecordManager
import com.timothy.searchemulator.ui.emulator.MovementRecordManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@InstallIn(ViewModelComponent::class)
@Module
abstract class Module{
    @Binds
    abstract fun bindMovementRecordManager(impl: MovementRecordManagerImpl): MovementRecordManager
}