import org.koin.dsl.module

val appModule = module {
    single { Database() }
    single { Game(get()) }
}