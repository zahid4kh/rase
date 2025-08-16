import androidx.compose.ui.window.WindowPosition.PlatformDefault.x
import androidx.compose.ui.window.WindowPosition.PlatformDefault.y
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
            _uiState.update {
                it.copy(showPlayButton = false, requestFocus = true)
            }
            var frameCounter = 0
            while(isActive){
                updateWorldPosition()
                removeOffscreenCoins()
                checkCollision()

                frameCounter ++
                if(frameCounter >= 90){
                    spawnCoins()
                    frameCounter = 0
                }

                delay(16)
            }
        }
    }

    fun stopGameLoop(){
        gameLoopJob?.cancel()
        gameLoopJob = null
        println("Game loop cancelled")
        _uiState.update {
            it.copy(showPlayButton = true)
        }
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
        println("List of coins: ${currentCoins.size}")
    }

    fun removeOffscreenCoins() {
        val currentCoins = _uiState.value.activeCoins
        val visibleCoins = currentCoins.filter { coin ->
            val screenY = coin.y + _uiState.value.worldOffsetY
            screenY < _uiState.value.screenHeight + 50f
        }
        if (currentCoins.size != visibleCoins.size) {
            println("Removed ${currentCoins.size - visibleCoins.size} coins")
        }
        _uiState.update { it.copy(activeCoins = visibleCoins) }
    }

    fun checkCollision(){
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
        val score: Int = 0
    )

    data class Coin(
        val x: Float = 0f,
        val y: Float = 0f
    )
}