package com.abrarshakhi.mishti.core.session.di

import com.abrarshakhi.mishti.core.session.data.DefaultModelSelector
import com.abrarshakhi.mishti.core.session.domain.ModelSelector
import com.abrarshakhi.mishti.core.session.ui.viewmodels.SessionsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val sessionModule = module {
    single<ModelSelector> { DefaultModelSelector(get(), get()) }
    viewModel { SessionsViewModel(get(), get(), get()) }
}
