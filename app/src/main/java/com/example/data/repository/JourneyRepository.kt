package com.example.data.repository

import com.example.data.db.NodeDao
import com.example.data.model.MediaType
import com.example.data.model.MilestoneNode
import com.example.data.model.NodeMedia
import com.example.data.model.NodeWithMedia
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

import com.example.data.model.TodoItem

class JourneyRepository(private val nodeDao: NodeDao) {

    val allNodes: Flow<List<MilestoneNode>> = nodeDao.getAllNodes()
    val allNodesWithMedia: Flow<List<NodeWithMedia>> = nodeDao.getAllNodesWithMedia()

    suspend fun getActiveNode(): MilestoneNode? = nodeDao.getActiveNode()

    suspend fun getNodeWithMedia(nodeId: Int): NodeWithMedia? = nodeDao.getNodeWithMediaById(nodeId)

    suspend fun prepopulateIfEmpty() {
        val count = nodeDao.getNodeCount()
        if (count != 365) {
            nodeDao.deleteAllNodes()
            val initialNodes = List(365) { i ->
                val day = i + 1
                MilestoneNode(
                    id = day,
                    title = "Day $day",
                    description = "Capture and express your achievements and evidence for this day on your climb up the progress mountain.",
                    isActive = (day == 1),
                    isCompleted = false,
                    unlockedAt = System.currentTimeMillis()
                )
            }
            nodeDao.insertNodes(initialNodes)
        }
    }

    suspend fun addProof(nodeId: Int, type: MediaType, content: String) {
        val node = nodeDao.getNodeById(nodeId) ?: return
        val media = NodeMedia(nodeId = nodeId, type = type, content = content)
        nodeDao.insertMedia(media)

        val now = System.currentTimeMillis()
        // If it was not completed before, set unlockedAt to now so midnight tracks from this first logged entry!
        val updatedNode = if (node.isActive && !node.isCompleted) {
            node.copy(isCompleted = true, unlockedAt = now)
        } else {
            node.copy(isCompleted = true)
        }
        nodeDao.updateNode(updatedNode)
    }

    suspend fun checkAndAdvanceProgress(): Boolean {
        val activeNode = nodeDao.getActiveNode() ?: return false
        if (activeNode.isCompleted) {
            val now = System.currentTimeMillis()
            if (isPastMidnight(activeNode.unlockedAt, now)) {
                // Deactivate current
                val deactivatedNode = activeNode.copy(isActive = false)
                nodeDao.updateNode(deactivatedNode)

                // Activate next
                val nextNodeId = activeNode.id + 1
                val nextNode = nodeDao.getNodeById(nextNodeId)
                if (nextNode != null) {
                    val activatedNode = nextNode.copy(
                        isActive = true, 
                        unlockedAt = now, 
                        isCompleted = false
                    )
                    nodeDao.updateNode(activatedNode)
                    return true
                }
            }
        }
        return false
    }

    private fun isPastMidnight(unlockedTime: Long, currentTime: Long): Boolean {
        val unlockedCal = Calendar.getInstance().apply { timeInMillis = unlockedTime }
        val currentCal = Calendar.getInstance().apply { timeInMillis = currentTime }

        val unlockYear = unlockedCal.get(Calendar.YEAR)
        val unlockDay = unlockedCal.get(Calendar.DAY_OF_YEAR)

        val currentYear = currentCal.get(Calendar.YEAR)
        val currentDay = currentCal.get(Calendar.DAY_OF_YEAR)

        return if (currentYear > unlockYear) {
            true
        } else {
            currentYear == unlockYear && currentDay > unlockDay
        }
    }

    suspend fun simulateMidnightForTesting(nodeId: Int) {
        val node = nodeDao.getNodeById(nodeId) ?: return
        if (node.isActive) {
            // Set unlockedAt to 25 hours ago, so it satisfies 'past midnight' instantly!
            val updatedNode = node.copy(unlockedAt = System.currentTimeMillis() - 25 * 60 * 60 * 1000)
            nodeDao.updateNode(updatedNode)
        }
    }

    suspend fun deleteMedia(mediaId: Long, nodeId: Int) {
        nodeDao.deleteMediaById(mediaId)
        val withMedia = nodeDao.getNodeWithMediaById(nodeId)
        if (withMedia == null || withMedia.mediaList.isEmpty()) {
            val node = nodeDao.getNodeById(nodeId)
            if (node != null) {
                nodeDao.updateNode(node.copy(isCompleted = false))
            }
        }
    }

    // To-Do Methods
    fun getTodoItemsForDate(dateString: String): Flow<List<TodoItem>> {
        return nodeDao.getTodoItemsForDate(dateString)
    }

    suspend fun findTodoByTitleAndDate(title: String, dateString: String): TodoItem? {
        return nodeDao.findTodoByTitleAndDate(title, dateString)
    }

    suspend fun insertTodoItem(todoItem: TodoItem): Long {
        return nodeDao.insertTodoItem(todoItem)
    }

    suspend fun updateTodoItem(todoItem: TodoItem) {
        nodeDao.updateTodoItem(todoItem)
    }

    suspend fun deleteTodoItem(todoItem: TodoItem) {
        nodeDao.deleteTodoItem(todoItem)
    }
}
