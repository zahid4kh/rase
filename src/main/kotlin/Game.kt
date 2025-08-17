import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class Game(
    private val database: Database,
) {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)
    private var gameLoopJob: Job? = null

    companion object {
        const val CAR_WIDTH = 30f
        const val CAR_HEIGHT = 50f
        const val MOVE_STEP = 10f
    }

    init {
        scope.launch {
            val settings = database.getSettings()
            withContext(Dispatchers.Main){

            }
            _uiState.update { it.copy(darkMode = settings.darkMode) }
        }
    }

    fun startGameLoop(){
        gameLoopJob = scope.launch {
            setBackground()
            _uiState.update {
                it.copy(showPlayButton = false, requestFocus = true)
            }
            var coinFrameCounter = 0
            var obstacleFrameCounter = 0
            while(isActive){
                updateWorldPosition()
                removeOffscreenCoins()
                removeOffScreenObstacles()
                checkCoinCollision()
                checkObstacleCollision()

                coinFrameCounter ++
                if(coinFrameCounter >= 90){
                    spawnCoins()
                    coinFrameCounter = 0
                }

                obstacleFrameCounter ++
                if(obstacleFrameCounter >= 180){
                    spawnObstacles()
                    obstacleFrameCounter = 0
                }

                delay(16)
            }
        }
    }

    fun pauseGame(){
        gameLoopJob?.cancel()
        gameLoopJob = null
        println("Game paused")
        _uiState.update { currentState ->
            currentState.copy(showPlayButton = true)
        }
        setBackground()
    }

    fun resetGame(){
        gameLoopJob?.cancel()
        gameLoopJob = null
        _uiState.update {
            currentState ->
            currentState.copy(
                score = 0,
                showPlayButton = true,
                activeCoins = emptyList(),
                activeObstacles = emptyList(),
                worldOffsetY = 0f,
                carX = currentState.screenWidth/2f - CAR_WIDTH/2,
                carY = currentState.screenHeight - 150f
            )
        }
        setBackground()
    }

    fun initializeGame(screenWidth: Float, screenHeight: Float) {
        _uiState.value = _uiState.value.copy(
            carX = screenWidth / 2f - CAR_WIDTH / 2f,
            carY = screenHeight - 150f,
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )
    }

    fun updateWorldPosition(){
        val currentPos = _uiState.value
        _uiState.update {
            it.copy(worldOffsetY = currentPos.worldOffsetY +2 )
        }
    }

    fun spawnCoins(){
        val range = 50..370
        val xPos = range.random()

        val yPos = -200f - _uiState.value.worldOffsetY

        val newCoin = Coin(x = xPos.toFloat(), y = yPos)
        val currentCoins = _uiState.value.activeCoins.toMutableList()

        currentCoins.add(newCoin)

        _uiState.update {
            it.copy(activeCoins = currentCoins)
        }
    }

    fun spawnObstacles(){
        val currentState = _uiState.value
        val obstaclePositions = (currentState.screenWidth.toInt() downTo 5 step 80).toList()
        val xPos = obstaclePositions.random().toFloat()
        val yPos = -200f - currentState.worldOffsetY

        val newObstacle = Obstacle(x = xPos, y = yPos)
        val currentObstacles = currentState.activeObstacles.toMutableList()

        currentObstacles.add(newObstacle)
        _uiState.update {
            it.copy(activeObstacles = currentObstacles)
        }
    }

    fun removeOffscreenCoins() {
        val currentCoins = _uiState.value.activeCoins
        val visibleCoins = currentCoins.filter { coin ->
            val screenY = coin.y + _uiState.value.worldOffsetY
            screenY < _uiState.value.screenHeight + 50f
        }
        if (currentCoins.size != visibleCoins.size) {
            //println("Removed ${currentCoins.size - visibleCoins.size} coins")
        }
        _uiState.update { it.copy(activeCoins = visibleCoins) }
    }

    fun removeOffScreenObstacles(){
        val currentObstacles = _uiState.value.activeObstacles
        val visibleObstacles = currentObstacles.filter { obstacle ->
            val screenY = obstacle.y + _uiState.value.worldOffsetY
            screenY < _uiState.value.screenHeight + 50f
        }
        if (currentObstacles.size != visibleObstacles.size) {
            println("Removed ${currentObstacles.size - visibleObstacles.size} obstacles")
        }
        _uiState.update { it.copy(activeObstacles = visibleObstacles) }
    }

    fun checkCoinCollision(){
        val currentState = _uiState.value
        val currentCoins = currentState.activeCoins
        val collisionThreshold = 15f

        val carCenter = Pair(
            currentState.carX + CAR_WIDTH/2,
            currentState.carY + CAR_HEIGHT/2
        )

        val collectedCoins = currentCoins.filter { coin ->
            val coinScreenY = coin.y + currentState.worldOffsetY
            abs(carCenter.first - coin.x) <= collisionThreshold &&
            abs(carCenter.second - coinScreenY) <= collisionThreshold
        }

        _uiState.update { it.copy(score = collectedCoins.size + it.score) }

        val visibleCoins = currentCoins.filter { coin->
            !collectedCoins.contains(coin)
        }
        _uiState.update { it.copy(activeCoins = visibleCoins) }
    }

    fun checkObstacleCollision(){
        val currentState = _uiState.value
        val currentObstacles = currentState.activeObstacles

        val carLeft = currentState.carX
        val carRight = currentState.carX + CAR_WIDTH
        val carTop = currentState.carY
        val carBottom = currentState.carY + CAR_HEIGHT

        for (obstacle in currentObstacles) {
            val obstacleScreenY = obstacle.y + currentState.worldOffsetY

            val obstacleLeft = obstacle.x
            val obstacleRight = obstacle.x + 80f
            val obstacleTop = obstacleScreenY
            val obstacleBottom = obstacleScreenY + 2f

            val xOverlap = carLeft < obstacleRight && carRight > obstacleLeft
            val yOverlap = carTop < obstacleBottom && carBottom > obstacleTop

            if (xOverlap && yOverlap) {
                resetGame()
                return
            }
        }
    }

    fun setBackground(){
        if (gameLoopJob?.isActive == true){
            _uiState.update { it.copy(background = Color.White) }
        }else{
            _uiState.update { it.copy(background = Color.LightGray) }
        }
    }

    fun moveCarUp() {
        if (gameLoopJob?.isActive != true) return

        val currentState = _uiState.value
        val newY = (currentState.carY - MOVE_STEP).coerceAtLeast(0f)
        _uiState.value = currentState.copy(carY = newY)
    }

    fun moveCarDown() {
        if (gameLoopJob?.isActive != true) return

        val currentState = _uiState.value
        val maxY = currentState.screenHeight - CAR_HEIGHT*2.5f
        val newY = (currentState.carY + MOVE_STEP).coerceAtMost(maxY)
        _uiState.value = currentState.copy(carY = newY)
    }

    fun moveCarLeft() {
        if (gameLoopJob?.isActive != true) return

        val currentState = _uiState.value
        val newX = (currentState.carX - MOVE_STEP).coerceAtLeast(0f)
        _uiState.value = currentState.copy(carX = newX)
    }

    fun moveCarRight() {
        if (gameLoopJob?.isActive != true) return

        val currentState = _uiState.value
        val maxX = currentState.screenWidth - CAR_WIDTH
        val newX = (currentState.carX + MOVE_STEP).coerceAtMost(if (maxX > 0) maxX else currentState.carX)
        _uiState.value = currentState.copy(carX = newX)
    }

    fun clearFocusRequest(){
        _uiState.update { it.copy(requestFocus = false) }
    }
    fun toggleDarkMode() {
        val newDarkMode = !_uiState.value.darkMode
        _uiState.value = _uiState.value.copy(darkMode = newDarkMode)

        scope.launch {
            val settings = database.getSettings()
            database.saveSettings(settings.copy(darkMode = newDarkMode))
        }
    }

    data class UiState(
        val darkMode: Boolean = false,
        val carX: Float = 0f,
        val carY: Float = 0f,
        val screenWidth: Float = 0f,
        val screenHeight: Float = 0f,
        val showPlayButton: Boolean = true,
        val requestFocus: Boolean = false,
        val worldOffsetY: Float = 0f,
        val lineSpacing: Float = 10f,
        val lineLength: Float = 10f,
        val activeCoins: List<Coin> = emptyList(),
        val activeObstacles: List<Obstacle> = emptyList(),
        val score: Int = 0,
        val background: Color = Color.LightGray
    )

    data class Coin(
        val x: Float = 0f,
        val y: Float = 0f
    )

    data class Obstacle(
        val x: Float = 0f,
        val y: Float = -10f
    )
}