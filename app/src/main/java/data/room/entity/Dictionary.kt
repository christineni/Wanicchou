package data.room.entity

import android.arch.persistence.room.*

@Entity(tableName = "Dictionary")
data class Dictionary (
    @ColumnInfo(name = "DictionaryName")
    val dictionaryName: String,

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "DictionaryID")
    val dictionaryID: Int = 0
)