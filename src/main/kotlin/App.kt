import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleOut
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import theme.AppTheme


@Composable
@Preview
fun App(
    viewModel: Game
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(uiState.requestFocus) {
        if(uiState.requestFocus){
            focusRequester.requestFocus()
            viewModel.clearFocusRequest()
        }

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
                        println("Key event detected: ${keyEvent.key}")
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            when (keyEvent.key) {
                                Key.W -> {
                                    println("W pressed!")
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

                                Key.Escape -> {
                                    viewModel.stopGameLoop()
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
                    size = Size(Game.CAR_WIDTH, Game.CAR_HEIGHT),
                    topLeft = Offset(animateX, animateY)
                )
            }

            AnimatedVisibility(
                visible = uiState.showPlayButton,
                exit = scaleOut(targetScale = 0f),
                modifier = Modifier.align(Alignment.Center)
            ){
                Button(
                    onClick = {viewModel.startGameLoop()},
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon.Hand)
                ){
                    Text("PLAY", fontWeight = FontWeight.Bold)
                }
            }

        }
    }
}