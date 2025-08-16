import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.key.*
import sun.swing.SwingUtilities2.drawRect
import theme.AppTheme


@Composable
@Preview
fun App(
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val animateY by animateFloatAsState(
        targetValue = uiState.carY,
        animationSpec = spring()
    )
    val animateX by animateFloatAsState(
        targetValue = uiState.carX,
        animationSpec = spring()
    )

    AppTheme(darkTheme = uiState.darkMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .focusRequester(focusRequester)
                    .focusable()
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            when (keyEvent.key) {
                                Key.W -> {
                                    viewModel.moveCarUp()
                                    return@onKeyEvent true
                                }

                                Key.S -> {
                                    viewModel.moveCarDown()
                                    return@onKeyEvent true
                                }

                                Key.A -> {
                                    viewModel.moveCarLeft()
                                    return@onKeyEvent true
                                }

                                Key.D -> {
                                    viewModel.moveCarRight()
                                    return@onKeyEvent true
                                }
                            }
                        }
                        false
                    }
            ) {
                if (size.width > 0 && size.height > 0 && uiState.screenWidth == 0f) {
                    viewModel.initializeGame(size.width, size.height)
                }

                for (i in size.width.toInt() downTo 5 step 80) {
                    drawLine(
                        color = Color.DarkGray,
                        start = Offset(i.toFloat(), 0f),
                        end = Offset(i.toFloat(), size.height),
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(10f, 10f)
                        )
                    )
                }

                drawRect(
                    color = Color.Black,
                    size = Size(MainViewModel.CAR_WIDTH, MainViewModel.CAR_HEIGHT),
                    topLeft = Offset(animateX, animateY)
                )
            }
        }
    }
}