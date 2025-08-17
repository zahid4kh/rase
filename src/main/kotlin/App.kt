import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
    val listOfObstacles = uiState.activeObstacles

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

    AppTheme {
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
                                    viewModel.pauseGame()
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

                drawRect(
                    color = uiState.background
                )

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

                for (obstacle in listOfObstacles){
                    drawObstacle(
                        x = obstacle.x,
                        y = obstacle.y + uiState.worldOffsetY
                    )
                }

                drawRect(
                    color = Color.Black,
                    size = Size(
                        Game.CAR_WIDTH * uiState.carSizeFactor,
                        Game.CAR_HEIGHT * uiState.carSizeFactor
                    ),
                    topLeft = Offset(animateX, animateY)
                )
            }
            Text(
                text = "Score: ${uiState.score}",
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                style = MaterialTheme.typography.titleLarge
            )

            Column(
                modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ){
                AnimatedVisibility(
                    visible = uiState.showPlayButton,
                    enter = slideInVertically(),
                    exit = scaleOut(targetScale = 0f),
                ){
                    Button(
                        onClick = {viewModel.startGameLoop()},
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .pointerHoverIcon(PointerIcon.Hand)
                    ){
                        Text(
                            text = if(uiState.isPaused) "Continue" else "PLAY",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                AnimatedVisibility(
                    visible = uiState.isPaused,
                    enter = slideInVertically(
                        animationSpec = tween(delayMillis = 400),
                        initialOffsetY = {it-800}
                    )
                ){
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ){
                        Text(
                            text = "Game is paused :)",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Thin
                        )

                        Menu(
                            uiState = uiState,
                            game = viewModel
                        )
                    }

                }
            }
        }
    }
}

@Composable
fun Menu(
    uiState: Game.UiState,
    game: Game
){
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth(0.8f),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.onSurface)
    ){
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            MenuItem(
                itemTitle = "Game Speed:",
                onValueChange = {newValue -> game.setNewGameSpeed(newValue) },
                value = uiState.gameSpeedFactor,
                valueRange = 1f..5f
            )
            MenuItem(
                itemTitle = "Car Size:",
                onValueChange = { newValue -> game.setNewCarSize(newValue) },
                value = uiState.carSizeFactor,
                valueRange = 1f..5f
            )
            MenuItem(
                itemTitle = "Car Speed:",
                onValueChange = { newValue -> game.setNewCarSpeed(newValue) },
                value = uiState.carSpeedFactor,
                valueRange = 1f..3f
            )
        }
    }
}

@Composable
fun MenuItem(
    itemTitle: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    onValueChange: (Float) -> Unit
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.padding(7.dp)
    ){
        Text(itemTitle, style = MaterialTheme.typography.labelMedium)

        Slider(
            value = value,
            onValueChange = {newValue -> onValueChange(newValue)},
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.weight(1f).pointerHoverIcon(PointerIcon.Hand)
        )
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

fun DrawScope.drawObstacle(x: Float, y: Float){
    drawRect(
        color = Color.Red,
        size = Size(80f, 2f),
        topLeft = Offset(x, y)
    )
}