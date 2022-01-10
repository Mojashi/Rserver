package solver.api
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.request.receiveText
import io.ktor.request.receive
import io.ktor.request.receiveStream
import solver.api.model.BoardPartId
import solver.api.model.BoardPart
import solver.api.model.Mark
import solver.api.model.Problem
import solver.api.model.Color
import solver.api.model.Pos
import solver.api.model.MarkShape
import solver.api.model.Direction
import solver.api.model.MirrorDirection
import solver.api.repo.BoardPartRepo
import solver.api.service.SolveService
import solver.api.service.SolverTimeoutException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

import kotlinx.serialization.Serializable
import kotlinx.atomicfu.AtomicInt
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.atomic.AtomicInteger
import ch.qos.logback.classic.Logger
import java.io.File

@Serializable
data class SolveRequest (
    val problem: Problem
)

fun main(args: Array<String>) {
    val queueCount: AtomicInteger = AtomicInteger(0)
    val mutex = Mutex()
    val solver = SolveService()
    
    val server = embeddedServer(Netty, port = 8080) {
        routing {
            get("/") {
                call.respondText("Hello World!", ContentType.Text.Plain)
            };
            get("/privacy-policy") {
                call.respondFile(File("policy.html"))
            }
            get("/random") {
                val problem = Problem (
                    listOf(Color.Blue,Color.Red,Color.Yellow,Color.Green).shuffled().map{cat ->
                        solver.bpRepo.parts.values.filter{it.category==cat}.first()!!.id
                    }.toTypedArray(),
                    Mark.random(),
                    (0..15).flatMap{x->(0..15).map{Pos(x,it)}}.shuffled().take(5).zip(listOf(Color.Blue, Color.Black,Color.Red,Color.Yellow,Color.Green))
                    .map{ (pos, color) ->color to pos}.toMap(), false
                )
                println("${problem.mark}")
                call.respondText(solver.pStringify(solver.getBoard(problem), problem))
            };
            post("/solve") {
                if(queueCount.get() == 10) {
                    return@post call.respondText("too many queue: $queueCount", ContentType.Text.Plain, HttpStatusCode.ServiceUnavailable)
                }
                val queueing = queueCount.addAndGet(1)
                log.info("queueing: $queueing")

                val req = Json.decodeFromString<SolveRequest>(call.receiveText())
                
                mutex.lock()
                try {
                    val hands = solver.Solve(req.problem)
                    return@post call.respondText(Json.encodeToString(hands))
                } catch (e: SolverTimeoutException) {
                    return@post call.respondText("solver timeout", ContentType.Text.Plain, HttpStatusCode.UnprocessableEntity)
                } finally {
                    mutex.unlock()
                    queueCount.addAndGet(-1)
                    log.info("queueing: $queueing")
                }
            }
        }
    }
    server.start(wait = true)
}