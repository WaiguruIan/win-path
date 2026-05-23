package com.example.data.db

import androidx.room.*
import com.example.data.model.MilestoneNode
import com.example.data.model.NodeMedia
import com.example.data.model.NodeWithMedia
import com.example.data.model.TodoItem
import kotlinx.coroutines.flow.Flow

@Dao
interface NodeDao {
    @Query("SELECT * FROM milestone_nodes ORDER BY id ASC")
    fun getAllNodes(): Flow<List<MilestoneNode>>

    @Transaction
    @Query("SELECT * FROM milestone_nodes ORDER BY id ASC")
    fun getAllNodesWithMedia(): Flow<List<NodeWithMedia>>

    @Query("SELECT * FROM milestone_nodes WHERE id = :id")
    suspend fun getNodeById(id: Int): MilestoneNode?

    @Query("SELECT * FROM milestone_nodes WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveNode(): MilestoneNode?

    @Transaction
    @Query("SELECT * FROM milestone_nodes WHERE id = :id")
    suspend fun getNodeWithMediaById(id: Int): NodeWithMedia?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: MilestoneNode)

    @Query("SELECT COUNT(*) FROM milestone_nodes")
    suspend fun getNodeCount(): Int

    @Query("DELETE FROM milestone_nodes")
    suspend fun deleteAllNodes()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNodes(nodes: List<MilestoneNode>)

    @Update
    suspend fun updateNode(node: MilestoneNode)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: NodeMedia)

    @Query("DELETE FROM node_media WHERE id = :mediaId")
    suspend fun deleteMediaById(mediaId: Long)

    // To-Do Queries
    @Query("SELECT * FROM todo_items WHERE dateString = :dateString ORDER BY id ASC")
    fun getTodoItemsForDate(dateString: String): Flow<List<TodoItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodoItem(todoItem: TodoItem)

    @Update
    suspend fun updateTodoItem(todoItem: TodoItem)

    @Delete
    suspend fun deleteTodoItem(todoItem: TodoItem)
}
