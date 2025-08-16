import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleOut
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition.PlatformDefault.x
import androidx.compose.ui.window.WindowPosition.PlatformDefault.y
import theme.AppTheme
import java.awt.SystemColor.text


@Composable
@Preview
fun App(
    viewModel: Game
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val listOfCoins = uiState.activeCoins

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
    val wrapOffset = uiState.worldOffsetY % 20f

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
                        //println("Key event detected: ${keyEvent.key}")
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            when (keyEvent.key) {
                                Key.W -> {
                                    //println("W pressed!")
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
                        start = Offset(i.toFloat(), wrapOffset),
                        end = Offset(i.toFloat(), size.height + wrapOffset),
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(uiState.lineLength, uiState.lineSpacing)
                        )
                    )
                }

                for (coin in listOfCoins){
                    drawCoin(
                        x = coin.x,
                        y = coin.y + uiState.worldOffsetY,
                        deg = 0f
                    )
                }

                drawRect(
                    color = Color.Black,
                    size = Size(Game.CAR_WIDTH, Game.CAR_HEIGHT),
                    topLeft = Offset(animateX, animateY)
                )
            }
            Text(
                text = uiState.score.toString(),
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                style = MaterialTheme.typography.titleLarge
            )

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

fun DrawScope.drawCoin(x: Float, y: Float, deg: Float){
    rotate(
        degrees = deg,
        pivot = Offset(x, y)
    ){
        drawCircle(
            color = Color.Yellow,
            radius = 10f,
            center = Offset(x, y)
        )
    }

}