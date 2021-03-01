package com.unifonic.noticesdk.di.modules

import androidx.lifecycle.ViewModelProvider
import com.unifonic.noticesdk.di.ViewModelFactory
import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    //Add more ViewModels here

//    @Binds
//    @IntoMap
//    @ViewModelKey(MediaViewModel::class)
//    internal abstract fun mediaViewModel(viewModel: MediaViewModel): ViewModel
}