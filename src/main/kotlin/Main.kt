@file:JvmName("Rase")
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import theme.AppTheme
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin


fun main() = application {
    startKoin {
        modules(appModule)
    }

    val viewModel = getKoin().get<Game>()
    val windowState = rememberWindowState(size = DpSize(400.dp, 600.dp))

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        alwaysOnTop = true,
        title = "Rase",
        resizable = false
    ) {

        AppTheme {
            App(
                viewModel = viewModel
            )
        }
    }
}