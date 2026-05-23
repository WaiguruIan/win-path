package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.MediaType
import com.example.data.model.MilestoneNode
import com.example.data.model.NodeWithMedia
import com.example.data.model.TodoItem
import com.example.data.repository.JourneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JourneyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: JourneyRepository

    private val prefs = application.getSharedPreferences("journey_theme_settings", android.content.Context.MODE_PRIVATE)
    val appPrefs = prefs // expose if needed, but VM handles most

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", false)) // default false = Light Mode
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _themeColorHex = MutableStateFlow(prefs.getString("theme_color_hex", "#C084FC") ?: "#C084FC")
    val themeColorHex: StateFlow<String> = _themeColorHex.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString("user_name", "Journey Pioneer") ?: "Journey Pioneer")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _avatarPresetIndex = MutableStateFlow(prefs.getInt("avatar_preset_index", 0))
    val avatarPresetIndex: StateFlow<Int> = _avatarPresetIndex.asStateFlow()

    private val _customAvatarUri = MutableStateFlow(prefs.getString("custom_avatar_uri", "") ?: "")
    val customAvatarUri: StateFlow<String> = _customAvatarUri.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            _isDarkMode.value = enabled
            prefs.edit().putBoolean("is_dark_mode", enabled).apply()
        }
    }

    fun setThemeColorHex(hex: String) {
        viewModelScope.launch {
            _themeColorHex.value = hex
            prefs.edit().putString("theme_color_hex", hex).apply()
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            _userName.value = name
            prefs.edit().putString("user_name", name).apply()
        }
    }

    fun setAvatarPresetIndex(index: Int) {
        viewModelScope.launch {
            _avatarPresetIndex.value = index
            // Clear custom avatar uri when selecting preset to ensure preset is primary
            _customAvatarUri.value = ""
            prefs.edit()
                .putInt("avatar_preset_index", index)
                .putString("custom_avatar_uri", "")
                .apply()
        }
    }

    fun setCustomAvatarUri(uri: String) {
        viewModelScope.launch {
            _customAvatarUri.value = uri
            prefs.edit().putString("custom_avatar_uri", uri).apply()
        }
    }

    val allNodes: StateFlow<List<MilestoneNode>>
    val allNodesWithMedia: StateFlow<List<NodeWithMedia>>

    private val _selectedNodeWithMedia = MutableStateFlow<NodeWithMedia?>(null)
    val selectedNodeWithMedia: StateFlow<NodeWithMedia?> = _selectedNodeWithMedia.asStateFlow()

    private val _isCheckingProgress = MutableStateFlow(false)
    val isCheckingProgress: StateFlow<Boolean> = _isCheckingProgress.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = JourneyRepository(database.nodeDao())

        allNodes = repository.allNodes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allNodesWithMedia = repository.allNodesWithMedia.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Prepopulate, then run progression evaluation
        viewModelScope.launch {
            _isCheckingProgress.value = true
            repository.prepopulateIfEmpty()
            repository.checkAndAdvanceProgress()
            _isCheckingProgress.value = false
        }
    }

    fun selectNode(nodeId: Int) {
        viewModelScope.launch {
            val details = repository.getNodeWithMedia(nodeId)
            _selectedNodeWithMedia.value = details
        }
    }

    fun clearSelectedNode() {
        _selectedNodeWithMedia.value = null
    }

    fun addProof(nodeId: Int, type: MediaType, content: String) {
        viewModelScope.launch {
            repository.addProof(nodeId, type, content)
            // Refresh selection detail if we are currently viewing it
            _selectedNodeWithMedia.value?.let { current ->
                if (current.node.id == nodeId) {
                    selectNode(nodeId)
                }
            }
        }
    }

    fun deleteMedia(mediaId: Long, nodeId: Int) {
        viewModelScope.launch {
            repository.deleteMedia(mediaId, nodeId)
            // Refresh detailed view state
            _selectedNodeWithMedia.value?.let { current ->
                if (current.node.id == nodeId) {
                    selectNode(nodeId)
                }
            }
        }
    }

    // A wonderful developer tool for Instant testing inside the Streaming Android Emulator!
    fun simulateMidnightAndCheck(nodeId: Int) {
        viewModelScope.launch {
            _isCheckingProgress.value = true
            repository.simulateMidnightForTesting(nodeId)
            repository.checkAndAdvanceProgress()
            _isCheckingProgress.value = false
        }
    }

    // To-Do Screen Methods
    fun getTodoItems(dateString: String): StateFlow<List<TodoItem>> {
        // Return a Flow backed StateFlow to avoid recomposition leaks
        val flow = repository.getTodoItemsForDate(dateString)
        return flow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun addTodoItem(title: String, dateString: String) {
        viewModelScope.launch {
            repository.insertTodoItem(TodoItem(title = title, dateString = dateString, isCompleted = false))
        }
    }

    fun toggleTodoItem(todo: TodoItem) {
        viewModelScope.launch {
            repository.updateTodoItem(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    fun deleteTodoItem(todo: TodoItem) {
        viewModelScope.launch {
            repository.deleteTodoItem(todo)
        }
    }
}
