package solver.api.service

import solver.api.model.Wall
import solver.api.model.Mark
import solver.api.model.Color
import solver.api.model.BoardPart
import solver.api.model.MarkObject
import solver.api.model.Hand
import solver.api.model.Pos
import solver.api.model.Mirror
import solver.api.model.Direction

data class RawHand(val color: Color, val pos: Pos)


class Cell {
    val walls:MutableMap<Direction, Boolean> = mutableMapOf(*Direction.values().map{it to false}.toTypedArray())
    var mirror:Mirror? = null
    var mark: Mark? = null
}

class CellBoard(
    val width: Int,
    val height: Int, 
) {
    val cells: Array<Array<Cell>> = Array(height){Array(width){Cell()}}

    constructor(bp: BoardPart, width: Int=16, height: Int=16): this(width, height) {
        bp.walls.forEach(::setWall)
        bp.mirrors.forEach(::setMirror)
        bp.marks.forEach(::setMark)
    }

    fun setWall(wall: Wall) {
        cells[wall.y][wall.x].walls[wall.dir] = true
        val v = wall.dir.getVec()
        val rw = Wall(wall.dir.reverse(), wall.x + v.second, wall.y + v.first)

        cells[wall.y][wall.x].walls[wall.dir] = true
        if(rw.y in 0..height-1 && rw.x in 0..width-1)
            cells[rw.y][rw.x].walls[rw.dir] = true
    }
    fun setMirror(mirror: Mirror) {
        cells[mirror.y][mirror.x].mirror = mirror
    }
    fun setMark(markObj: MarkObject) {
        cells[markObj.y][markObj.x].mark = Mark(markObj.shape, markObj.color)
    }
}

fun parseHands(bp: BoardPart, poss: Map<Color, Pos>, rhands: List<RawHand>): List<Hand> {
    var curPoss = poss
    var cellBoard = CellBoard(bp)
    var hands:MutableList<Hand> = mutableListOf()
    println("$poss")

    rhands.forEach { hand ->
        println("${hand} ")
        val d = Direction.values().map{dir -> 
            dir to slide(cellBoard, curPoss, Hand(hand.color, dir))?.last()
        }.find { (_, lastPos) -> 
            lastPos?.let{it==hand.pos} ?: false
        }
        if(d == null)
            throw RuntimeException("invalid hands $hand")
        
        curPoss = mapOf(*curPoss.map{ (color, pos) ->
            color to (if(color == hand.color) d.second!! else pos)
        }.toTypedArray())
        hands += Hand(hand.color, d.first)
    }

    return hands
}

fun slide(cells: CellBoard, poss: Map<Color, Pos>, hand: Hand): List<Pos>? {
    var cur:Pos = poss[hand.color] ?: return null
    var history = mutableListOf(cur)
    var curDir = hand.dir

    fun collid():Boolean {
        val vec = curDir.getVec()
        val nex = Pos(cur.x + vec.second, cur.y + vec.first)
        val wallCol = cells.cells[cur.y][cur.x].walls[curDir] ?: false
        val pieceCol = poss.any{(color,pos)->
            color != hand.color && nex == pos
        }
        return wallCol || pieceCol
    }

    val al = mutableSetOf<Pos>()

    while(!collid()) {
        if(al.contains(cur)) return null
        al.add(cur)
        val vec = curDir.getVec()
        cur = Pos((cur.x + vec.second+16)%16, (cur.y + vec.first+16)%16)
        cells.cells[cur.y][cur.x].mirror?.let { 
            curDir = it.dir.reflect(curDir)
            history += cur
        }
    }

    return history + cur
}