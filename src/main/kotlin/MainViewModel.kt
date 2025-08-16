import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val database: Database,
) {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)

    companion object {
        const val CAR_WIDTH = 30f
        const val CAR_HEIGHT = 50f
        const val MOVE_STEP = 10f
    }

    init {
        scope.launch {
            val settings = database.getSettings()
            _uiState.value = _uiState.value.copy(
                darkMode = settings.darkMode,
            )
        }
    }

    fun toggleDarkMode() {
        val newDarkMode = !_uiState.value.darkMode
        _uiState.value = _uiState.value.copy(darkMode = newDarkMode)

        scope.launch {
            val settings = database.getSettings()
            database.saveSettings(settings.copy(darkMode = newDarkMode))
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

    fun moveCarUp() {
        val currentState = _uiState.value
        val newY = (currentState.carY - MOVE_STEP).coerceAtLeast(0f)
        _uiState.value = currentState.copy(carY = newY)
    }

    fun moveCarDown() {
        val currentState = _uiState.value
        val maxY = currentState.screenHeight - CAR_HEIGHT*2.5f
        val newY = (currentState.carY + MOVE_STEP).coerceAtMost(maxY)
        _uiState.value = currentState.copy(carY = newY)
    }

    fun moveCarLeft() {
        val currentState = _uiState.value
        val newX = (currentState.carX - MOVE_STEP).coerceAtLeast(0f)
        _uiState.value = currentState.copy(carX = newX)
    }

    fun moveCarRight() {
        val currentState = _uiState.value
        val maxX = currentState.screenWidth - CAR_WIDTH
        val newX = (currentState.carX + MOVE_STEP).coerceAtMost(if (maxX > 0) maxX else currentState.carX)
        _uiState.value = currentState.copy(carX = newX)
    }

    data class UiState(
        val darkMode: Boolean = false,
        val carX: Float = 0f,
        val carY: Float = 0f,
        val screenWidth: Float = 0f,
        val screenHeight: Float = 0f
    )
}