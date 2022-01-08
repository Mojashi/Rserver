package solver.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Pos (
        val x: Int,
        val y: Int,
)


@Serializable
data class Problem (
    val parts: Array<BoardPartId>,
    val mark: Mark,
    val piecePoss: Map<Color, Pos>,
    val torus: Boolean,
) 