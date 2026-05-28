package com.example.ui.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.TodoNotificationHelper
import com.example.data.db.AppDatabase
import com.example.data.model.MediaType
import com.example.data.model.MilestoneNode
import com.example.data.model.NodeWithMedia
import com.example.data.model.TodoItem
import com.example.data.repository.JourneyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private val _cropScale = MutableStateFlow(prefs.getFloat("avatar_crop_scale", 1f))
    val cropScale: StateFlow<Float> = _cropScale.asStateFlow()

    private val _cropOffsetX = MutableStateFlow(prefs.getFloat("avatar_crop_offset_x", 0f))
    val cropOffsetX: StateFlow<Float> = _cropOffsetX.asStateFlow()

    private val _cropOffsetY = MutableStateFlow(prefs.getFloat("avatar_crop_offset_y", 0f))
    val cropOffsetY: StateFlow<Float> = _cropOffsetY.asStateFlow()

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

    fun updateCropSettings(scale: Float, offsetX: Float, offsetY: Float) {
        viewModelScope.launch {
            _cropScale.value = scale
            _cropOffsetX.value = offsetX
            _cropOffsetY.value = offsetY
            prefs.edit()
                .putFloat("avatar_crop_scale", scale)
                .putFloat("avatar_crop_offset_x", offsetX)
                .putFloat("avatar_crop_offset_y", offsetY)
                .apply()
        }
    }

    fun removeCustomAvatar() {
        viewModelScope.launch {
            _customAvatarUri.value = ""
            prefs.edit().putString("custom_avatar_uri", "").apply()
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

    fun addTodoItem(title: String, dateString: String, onAdded: ((TodoItem) -> Unit)? = null) {
        viewModelScope.launch {
            val trimmedTitle = title.trim()
            if (trimmedTitle.isEmpty()) return@launch

            // Check if duplicate exists on this date
            val existing = repository.findTodoByTitleAndDate(trimmedTitle, dateString)
            if (existing != null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        "You have already added that item to the to-do list.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@launch
            }

            // Attempt to auto-parse time (e.g. "at 9:00 AM")
            val parsedTime = TodoNotificationHelper.parseTimePlaceholder(trimmedTitle)
            val newTodo = if (parsedTime != null) {
                TodoItem(
                    title = trimmedTitle,
                    dateString = dateString,
                    isCompleted = false,
                    alertTime = parsedTime.parsedTime24h,
                    isAlertEnabled = true
                )
            } else {
                TodoItem(
                    title = trimmedTitle,
                    dateString = dateString,
                    isCompleted = false
                )
            }

            val newId = repository.insertTodoItem(newTodo)
            val savedTodo = newTodo.copy(id = newId)
            
            // Schedule the alarm if parsed time was found
            if (savedTodo.isAlertEnabled && savedTodo.alertTime != null) {
                TodoNotificationHelper.scheduleTodoAlarm(getApplication(), savedTodo)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    getApplication(),
                    "Item added to the list!",
                    Toast.LENGTH_SHORT
                ).show()
                onAdded?.invoke(savedTodo)
            }
        }
    }

    fun toggleTodoItem(todo: TodoItem) {
        viewModelScope.launch {
            val updated = todo.copy(isCompleted = !todo.isCompleted)
            repository.updateTodoItem(updated)
            if (updated.isCompleted) {
                TodoNotificationHelper.cancelTodoAlarm(getApplication(), updated)
            } else if (updated.isAlertEnabled) {
                TodoNotificationHelper.scheduleTodoAlarm(getApplication(), updated)
            }
        }
    }

    fun deleteTodoItem(todo: TodoItem) {
        viewModelScope.launch {
            TodoNotificationHelper.cancelTodoAlarm(getApplication(), todo)
            repository.deleteTodoItem(todo)
        }
    }

    fun updateTodoItemAlarm(todo: TodoItem, title: String, alertTime: String?, offsetMinutes: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            val updated = todo.copy(
                title = title.trim(),
                alertTime = alertTime,
                alertOffsetMinutes = offsetMinutes,
                isAlertEnabled = isEnabled
            )
            repository.updateTodoItem(updated)
            if (isEnabled && alertTime != null) {
                TodoNotificationHelper.scheduleTodoAlarm(getApplication(), updated)
            } else {
                TodoNotificationHelper.cancelTodoAlarm(getApplication(), updated)
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    getApplication(),
                    if (isEnabled) "Alarm configuration saved!" else "Alarm notification muted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
