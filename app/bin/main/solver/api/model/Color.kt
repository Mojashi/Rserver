package solver.api.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Color{
    @SerialName("green") Green,
    @SerialName("yellow") Yellow,
    @SerialName("red") Red,
    @SerialName("blue") Blue,
    @SerialName("black") Black,
    @SerialName("any") Any;
}