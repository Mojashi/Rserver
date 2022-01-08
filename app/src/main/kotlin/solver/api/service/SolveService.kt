package solver.api.service
import solver.api.model.BoardPartId
import solver.api.model.BoardPart
import solver.api.model.Mark
import solver.api.model.Hand
import solver.api.model.Problem
import solver.api.model.Direction
import solver.api.model.Color
import solver.api.model.MirrorDirection
import solver.api.model.Pos
import solver.api.repo.BoardPartRepo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import io.ktor.features.BadRequestException
import java.io.InputStream
import java.util.Scanner
import java.util.concurrent.TimeUnit
import java.util.concurrent.ConcurrentLinkedQueue


class SolverTimeoutException() : Exception("solver timeout")

class SolveService() {
    val bpRepo = BoardPartRepo()

    fun getBoard(problem: Problem): BoardPart {
        val parts = problem.parts.map{
            if(problem.torus) bpRepo.parts[it]
            else bpRepo.parts[it]?.setTopSideWall(8)?.setLeftSideWall(8)
        }.filterNotNull()
        if(parts.size != 4) throw BadRequestException("insufficient num of parts")

        val moves = listOf(0 to 0, 0 to 8, 8 to 8, 8 to 0)
        val rotated = parts.mapIndexed { index,it -> 
            it.rotate(index).move(moves[index].first,moves[index].second) 
        }
        return rotated.reduce{sum,it -> sum.concat(it)}
    }

    fun Solve(problem: Problem): List<Hand> {
        val board = getBoard(problem)
        val instr = pStringify(board, problem)

        println(instr)

        val builder = ProcessBuilder("timeout", "10", "solver/solver_g")
        builder.redirectErrorStream(true)
        val r = builder.start()
        r.outputStream.write(instr.toByteArray())
        r.outputStream.flush()

        val outstr = r.inputStream.bufferedReader().readText()
        println(outstr)

        if(outstr == "") throw SolverTimeoutException()

        val rawHands = outstr.split("\n").drop(1).map{it.trim()}.filter { it!="" }.map{
            val terms = it.trim().split(" ").map{it.toInt()}
            if(terms.size != 3) throw RuntimeException("invalid output")

            val color = Color.values().find{it.ordinal == terms[0]}
            if(color == null) throw RuntimeException("invalid output: unknown color")

            RawHand(color, Pos(terms[2], terms[1]))
        }
        
        return parseHands(board, problem.piecePoss, rawHands)
    }

    fun bpStringify(part: BoardPart): String {
        val sb = StringBuilder()

        part.walls.forEach {
            sb.append(
                    "${it.y} ${it.x} ${when(it.dir) {
                Direction.UP -> 0
                Direction.RIGHT -> 1
                Direction.DOWN -> 2
                Direction.LEFT -> 3
            }}\n")
        }
        sb.append("-1 -1 -1\n")

        part.mirrors.forEach {
            sb.append(
            "${it.y} ${it.x} ${it.color.ordinal} ${when(it.dir) {
                MirrorDirection.RevSlash -> 0
                MirrorDirection.Slash -> 1
            }}\n")
        }
        sb.append("-1 -1 -1 -1\n")

        return sb.toString()
    }

    fun findGoal(part: BoardPart, problem: Problem): Pos? {
        val mo = part.marks.find { Mark(it.shape,it.color) == problem.mark }
        if(mo != null) {
            return Pos(mo.x, mo.y)
        }
        return null
    }

    fun pStringify(board: BoardPart, problem: Problem): String {
        val goal = findGoal(board, problem)
        goal ?: throw BadRequestException("goal doesnt exist")

        if(problem.piecePoss.size != 5) 
            throw BadRequestException("insufficient number of pieces")

        val sb = StringBuilder()

        sb.append(bpStringify(board))

        problem.piecePoss.toList().sortedBy { it.first.ordinal }.forEach {(_,pos) ->
            sb.append("${pos.y} ${pos.x}\n")
        }
        sb.append("${if(problem.mark.color==Color.Any) -1 else problem.mark.color.ordinal}\n")
        sb.append("${goal.y} ${goal.x}\n")
        
        return sb.toString()
    }
}