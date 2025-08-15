@file:JvmName("Rase")
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import theme.AppTheme
import java.awt.Dimension
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin
import rase.resources.*


fun main() = application {
    startKoin {
        modules(appModule)
    }

    val viewModel = getKoin().get<MainViewModel>()

    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(size = DpSize(500.dp, 800.dp)),
        alwaysOnTop = true,
        title = "Rase",
        icon = null
    ) {
        window.minimumSize = Dimension(500, 800)

        AppTheme {
            App(
                viewModel = viewModel
            )
        }
    }
}