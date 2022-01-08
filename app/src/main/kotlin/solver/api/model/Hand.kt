package solver.api.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import solver.api.model.Direction
import solver.api.model.Color

@Serializable
data class Hand (
    val color: Color,
    val dir: Direction
)