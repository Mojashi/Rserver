package solver.api.model

import java.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MirrorDirection {
    @SerialName("/") Slash {
        override fun reflect(dir: Direction):Direction = when(dir) {
            Direction.UP -> Direction.RIGHT
            Direction.RIGHT -> Direction.UP
            Direction.DOWN -> Direction.LEFT
            Direction.LEFT -> Direction.DOWN
        }
    },
    @SerialName("\\") RevSlash{
        override fun reflect(dir: Direction):Direction = when(dir) {
            Direction.UP -> Direction.LEFT
            Direction.RIGHT -> Direction.DOWN
            Direction.DOWN -> Direction.RIGHT
            Direction.LEFT -> Direction.UP
        }
    };
    abstract fun reflect(dir: Direction):Direction
}

@Serializable
enum class Direction {
    @SerialName("up")
    UP {
        override fun reverse() = DOWN
        override fun getVec() = Pair(-1, 0)
        override fun rot90() = RIGHT
    },
    @SerialName("left")
    LEFT {
        override fun reverse() = RIGHT
        override fun getVec() = Pair(0, -1)
        override fun rot90() = UP
    },
    @SerialName("right")
    RIGHT {
        override fun reverse() = LEFT
        override fun getVec() = Pair(0, 1)
        override fun rot90() = DOWN
    },
    @SerialName("down")
    DOWN {
        override fun reverse() = UP
        override fun getVec() = Pair(1, 0)
        override fun rot90() = LEFT
    };

    abstract fun reverse(): Direction
    abstract fun getVec(): Pair<Int, Int>
    abstract fun rot90(): Direction
}

@Serializable
data class Mirror(
        val color: Color,
        val dir: MirrorDirection,
        val x: Int,
        val y: Int,
)

@Serializable
data class Wall(
        val dir: Direction,
        val x: Int,
        val y: Int,
) {
    fun reverse(): Wall {
        val v = dir.getVec()
        return Wall(dir.reverse(), x + v.second, y + v.first)
    }
}

@Serializable
enum class MarkShape {
    @SerialName("hexagon") Hexagon,
    @SerialName("circle") Circle,
    @SerialName("rectangle") Rectangle,
    @SerialName("triangle") Triangle,
    @SerialName("any") Any,
}

@Serializable
data class Mark(
        val shape: MarkShape,
        val color: Color,
)

@Serializable
data class MarkObject(
        val shape: MarkShape,
        val color: Color,
        val x: Int,
        val y: Int,
)

@JvmInline @Serializable value class BoardPartId(val id: Int)

@Serializable
data class BoardPart(
        val id: BoardPartId,
        val mirrors: List<Mirror>,
        val walls: List<Wall>,
        val marks: List<MarkObject>,
) {

    fun setLeftSideWall(height: Int): BoardPart {
        val sideWall = (0..height-1).map{Wall(Direction.LEFT, 0, it)}
        return BoardPart(
            BoardPartId(0),
            mirrors,
            walls + sideWall,
            marks,
        )
    }

    fun setTopSideWall(width: Int): BoardPart {
        val sideWall = (0..width-1).map{Wall(Direction.UP, it, 0)}
        return BoardPart(
            BoardPartId(0),
            mirrors,
            walls + sideWall,
            marks,
        )
    }
    
    fun move(dy: Int, dx: Int): BoardPart {
        return BoardPart(
                BoardPartId(0),
                mirrors.map { Mirror(it.color, it.dir, it.x + dx, it.y + dy) },
                walls.map { Wall(it.dir, it.x + dx, it.y + dy) },
                marks.map { MarkObject(it.shape, it.color, it.x + dx, it.y + dy) },
        )
    }

    fun concat(p: BoardPart): BoardPart {
        return BoardPart(
                BoardPartId(0),
                mirrors + p.mirrors,
                walls + p.walls,
                marks + p.marks,
        )
    }

    fun rotate(rot: Int, width: Int = 8): BoardPart {
        return (1..rot%4).fold(this){ it,_ -> it.rotate90At(width) }
    } 

    fun rotate90At(width: Int = 8): BoardPart {
        val rotWalls = walls.map { Wall(it.dir.rot90(), width - it.y - 1, it.x) }
        val rotMirrors =
                mirrors.map {
                    Mirror(
                            it.color,
                            when (it.dir) {
                                MirrorDirection.Slash -> MirrorDirection.RevSlash
                                MirrorDirection.RevSlash -> MirrorDirection.Slash
                            },
                            width - it.y - 1,
                            it.x
                    )
                }
        val rotMarks = marks.map { MarkObject(it.shape, it.color, width - it.y - 1, it.x) }
        return BoardPart(BoardPartId(0), rotMirrors, rotWalls, rotMarks)
    }
}


const val BoardPartNum = 16
