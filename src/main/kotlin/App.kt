import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import theme.AppTheme


@Composable
@Preview
fun App(
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val carWidth = 30f
    val carHeight = 50f

    var carX by remember { mutableStateOf(200f - carWidth / 2f)}
    var carY by remember { mutableStateOf(500f)}
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AppTheme(darkTheme = uiState.darkMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ){
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .focusRequester(focusRequester)
                    .focusable()
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            when (keyEvent.key) {
                                Key.W -> {
                                    carY -= 5f
                                    return@onKeyEvent true
                                }
                                Key.S -> {
                                    carY += 5f
                                    return@onKeyEvent true
                                }
                            }
                        }
                        false
                    }
            ){
                drawRect(
                    color = Color.Black,
                    size = Size(carWidth, carHeight),
                    topLeft = Offset(carX, carY)
                )
            }
        }
    }
}