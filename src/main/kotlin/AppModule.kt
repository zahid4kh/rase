import org.koin.dsl.module

val appModule = module {
    single { Game() }
}