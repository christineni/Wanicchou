package data.room.dao

import androidx.room.Dao
import androidx.room.Query
import data.room.entity.MatchType

@Dao
interface MatchTypeDao : BaseDao<MatchType> {
    @Query("""
        SELECT mt.TemplateString
        FROM MatchType mt
        WHERE mt.MatchTypeBitmask = :bitmask """)
    fun getTemplateString(bitmask : Long): String

    @Query( """
        SELECT mt.*
        FROM MatchType mt""")
    fun getAllMatchTypes() : List<MatchType>
}