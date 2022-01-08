package solver.api.service

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import solver.api.repo.BoardPartRepo
import solver.api.model.BoardPart
import solver.api.model.Pos
import solver.api.model.BoardPartId
import solver.api.model.Color
import solver.api.model.Hand
import solver.api.model.Direction


val bpRepo = BoardPartRepo()
val bp:BoardPart = bpRepo.parts[BoardPartId(1)]!!.setTopSideWall(8).setLeftSideWall(8).rotate(2).setTopSideWall(8).setLeftSideWall(8).rotate(2)
val cellBoard = CellBoard(bp, 8, 8)
class SolverTest {
    @Test
    fun testAdd() {
        assertEquals(3, 1 + 2);
    }
    @Test
    fun checkSlide1() {
        val his = slide(cellBoard, mapOf(Color.Blue to Pos(2, 2)), Hand(Color.Blue, Direction.RIGHT))
        assertEquals(his?.last(), Pos(7,2))
    }
    @Test
    fun checkSlide2() {
        val his = slide(cellBoard, mapOf(Color.Blue to Pos(1, 1)), Hand(Color.Blue, Direction.RIGHT))
        assertEquals(his?.last(), Pos(4,0))
    }
}