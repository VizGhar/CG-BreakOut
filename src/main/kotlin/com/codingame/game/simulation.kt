package com.codingame.game

import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

private const val maxAbsBallAngle = 70
private const val minAbsBallAngle = 10
const val paddleWidth = 64
const val paddleHeight = 32
const val ballWidth = 16
const val ballHeight = 16
var paddleCenterPosition = Position(640 / 2, 16)
var ballCenterPosition = Position(640 / 2, 32 + 8)
var ballAngle = minAbsBallAngle
var blocks = listOf<Block>()

private fun changeBallDirection(by: Int) {
    ballAngle = when {
        by < 0 && ballAngle + by < minAbsBallAngle -> -minAbsBallAngle
        by > 0 && ballAngle + by > -minAbsBallAngle -> minAbsBallAngle
        by < 0 && ballAngle + by < -maxAbsBallAngle -> -maxAbsBallAngle
        by > 0 && ballAngle + by > maxAbsBallAngle -> maxAbsBallAngle
        else -> ballAngle + by
    }
}

data class Position(val x: Int, val y: Int)
data class Block(
    val id: Int,
    val color: BreakoutColor,
    var lives: Int = 3,
    val x: Int = (id % 10) * 64,
    val y: Int = (id / 10 + 8) * 32,
    val width: Int = 64,
    val height: Int = 32
)
data class SimulationPoint(val position: Position, val hitObstacleId: Int?)

private val horizontal = List(9) { (it + 1) * 64 }
private val vertical = List(8) { (it + 7) * 32 }

private data class Note(val block: Block, val dist: Double, val hitPoint: Position)

fun simulate(blocks: List<Block>): List<SimulationPoint> {
    val obstacles = blocks.toMutableList()
    val visitedPoints = mutableListOf<SimulationPoint>()
    var currentPos = ballCenterPosition
    var angleRad = Math.toRadians(ballAngle.toDouble())

    do {
        val corners = listOf(
            Position(currentPos.x - 8, currentPos.y + 8),
            Position(currentPos.x + 8, currentPos.y + 8),
            Position(currentPos.x - 8, currentPos.y - 8),
            Position(currentPos.x + 8, currentPos.y - 8)
        )

        val candidates = mutableListOf<Note>()

        for (corner in corners) {
            for (obsY in horizontal) {
                val dY = obsY - corner.y
                val dX = dY * tan(angleRad)
                val dist = dY / cos(angleRad)
                obstacles.firstOrNull { obstacle -> corner.y + dY == obstacle.y && obstacle.x.toDouble() in corner.x + dX.. corner.x + dX + 64.0 }
                    ?.let { candidates += Note(it, dist, Position((corner.x + dX).roundToInt(), corner.y + dY)) }
            }

            for (obsX in vertical) {
                val dX = obsX - corner.x
                val dY = dX / tan(angleRad)
                val dist = dX / sin(angleRad)
                obstacles.firstOrNull { obstacle -> corner.x + dX == obstacle.x && obstacle.y.toDouble() in corner.y + dY..corner.y + dY + 32.0 }
                    ?.let { candidates += Note(it, dist, Position(corner.x + dX, (corner.y + dY).roundToInt())) }
            }

        }

        val winner = candidates.minByOrNull { it.dist }

        if (winner == null) {
            // hit a wall <-
        } else {
            visitedPoints += SimulationPoint(winner.hitPoint, winner.block.id)
        }

        currentPos = Position(0, 0)
    } while (currentPos.y > 0)

    return visitedPoints
}
