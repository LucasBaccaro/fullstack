package com.baccaro.lucas.di

import com.baccaro.lucas.authentication.remote.AuthService
import com.baccaro.lucas.core.KtorApi
import com.baccaro.lucas.core.KtorApiImpl
import com.baccaro.lucas.authentication.remote.AuthRepository
import com.baccaro.lucas.authentication.presentation.AuthViewModel
import com.baccaro.lucas.home.remote.TopicService
import com.baccaro.lucas.home.remote.TopicsRepository
import com.baccaro.lucas.home.presentation.TopicsViewModel
import com.baccaro.lucas.profile.remote.ProfileService
import com.baccaro.lucas.profile.remote.ProfileRepository
import com.baccaro.lucas.profile.presentation.ProfileViewModel
import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json
import com.baccaro.lucas.conversation.presentation.ConversationViewModel
import com.baccaro.lucas.conversation.remote.ConversationRepository
import com.baccaro.lucas.conversation.remote.ConversationService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

import com.baccaro.lucas.progress.remote.ProgressRepository
import com.baccaro.lucas.progress.remote.ProgressService

val appModule = module {
    single { Settings() }
    single<KtorApi> { KtorApiImpl() }
    single { AuthService(get(), get()) }
    single { AuthRepository(get(), get()) }
    single { TopicService(get(), get()) }
    single { TopicsRepository(get(), get()) }
    singleOf(::TopicsViewModel)
    single { ProfileService(get(), get()) }
    single { ProfileRepository(get(), get()) }
    singleOf(::ProfileViewModel)
    singleOf(::AuthViewModel)
    single { ConversationService(get(), get()) }
    single { ConversationRepository(get(), get()) }
    single { ProgressService(get(),get()) }
    single { ProgressRepository(get(),get())}
    singleOf(::ConversationViewModel)
    single { Json { ignoreUnknownKeys = true } }
}
