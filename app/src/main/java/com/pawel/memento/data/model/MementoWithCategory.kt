package com.pawel.memento.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class MementoWithCategory(
    @Embedded val memento: Memento,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: Category?
)
