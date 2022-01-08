package solver.api.repo

import solver.api.model.BoardPartId
import solver.api.model.BoardPart
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.collections.mapOf

class BoardPartRepo {
    val parts: Map<BoardPartId, BoardPart> 
    init {
        parts = mapOf(*(1..16).map {
            BoardPartId(it) to Json.decodeFromString<BoardPart>(File("boards/b$it.json").readText())
        }.toTypedArray())
    }

}