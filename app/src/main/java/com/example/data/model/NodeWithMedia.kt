package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class NodeWithMedia(
    @Embedded val node: MilestoneNode,
    @Relation(
        parentColumn = "id",
        entityColumn = "nodeId"
    )
    val mediaList: List<NodeMedia>
)
