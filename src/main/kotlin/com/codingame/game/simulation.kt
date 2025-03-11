package com.codingame.game

import kotlin.math.*

private const val screenHeight = 480
private const val screenWidth = 640
private const val maxAbsBallAngle = 70
private const val minAbsBallAngle = 10
private const val paddleWidth = 64
private const val paddleHeight = 32
private const val ballWidth = 16
private const val ballHeight = 16

var paddlePosition = Position((screenWidth - paddleWidth) / 2, screenHeight - paddleHeight)
var ballPosition = Position((screenWidth - ballWidth) / 2, screenHeight - paddleHeight - ballHeight - 1)
var ballAngle = 0
var blocks = listOf<Obstacle.Block>()

private val screenBlock = Obstacle.Screen
private val paddleBlock get() = Obstacle.Paddle(paddlePosition.x, paddlePosition.y)
private val obstacles get() = blocks + screenBlock + paddleBlock

private val precomputedHitBoxes : Map<Position, List<Position>> =
    (0..screenWidth)
        .map { x -> (0..screenHeight).map { y -> Position(x, y) to listOf(
            Position(x, y),
            Position(x + ballWidth, y),
            Position(x, y + ballHeight),
            Position(x + ballWidth, y + ballHeight)
        ) } }
        .flatten()
        .toMap()

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

sealed class Obstacle(val x: Int, val y: Int, val width: Int, val height: Int) {
    class Block(val id: Int, val color: BreakoutColor, var lives: Int = 3,
                x: Int = (id % 10) * 64, y: Int = (id / 10) * 32) : Obstacle(x, y, 64, 32)
    data object Screen : Obstacle(0, 0, screenWidth, screenHeight)
    class Paddle(x: Int, y: Int) : Obstacle(x, y, paddleWidth, paddleHeight)
}

data class SimulationPoint(
    val position: Position,
    val hitBlock: List<Obstacle>,
    val distance: Int
)

private fun findCollisionWithBorder(startX: Int, startY: Int, angle: Int): Position {
    val rad = Math.toRadians(angle.toDouble())
    val dx = sin(rad)
    val dy = -cos(rad)

    val tRight = if (dx > 0) (screenWidth - startX) / dx else Double.POSITIVE_INFINITY
    val tLeft = if (dx < 0) -startX / dx else Double.POSITIVE_INFINITY
    val tBottom = if (dy > 0) (screenHeight - startY) / dy else Double.POSITIVE_INFINITY
    val tTop = if (dy < 0) -startY / dy else Double.POSITIVE_INFINITY

    val t = minOf(tRight, tLeft, tBottom, tTop)
    return Position((startX + dx * t).roundToInt(), (startY + dy * t).roundToInt())
}

private fun bresenhamLine(from: Position, to: Position): List<Position> {
    val points = mutableListOf<Position>()

    var x = from.x
    var y = from.y
    val dx = abs(to.x - from.x)
    val dy = abs(to.y - from.y)
    val sx = if (from.x < to.x) 1 else -1
    val sy = if (from.y < to.y) 1 else -1
    var err = dx - dy

    while (true) {
        points.add(Position(x, y))
        if (x == to.x && y == to.y) break
        val e2 = 2 * err
        if (e2 > -dy) {
            err -= dy
            x += sx
        }
        if (e2 < dx) {
            err += dx
            y += sy
        }
    }

    return points
}

data class SimulationMetadata(
    val simulationPoints: List<SimulationPoint>,
    val totalDistance: Int,
    val continueGame: Boolean
)

fun simulateGame(): SimulationMetadata {
    val simulation = mutableListOf<SimulationPoint>()
    var totalDistance = 0
    do {
        val target = findCollisionWithBorder(ballPosition.x, ballPosition.y, ballAngle)
        val trajectory = bresenhamLine(ballPosition, target)
        for (i in 1..<trajectory.size) {
            val point = trajectory[i]
            val previousPoint = trajectory[i - 1]
            val hitPoints = precomputedHitBoxes[point] ?: throw IllegalStateException("no hitpoints at $point")

            val hitCandidates = obstacles.filter { obstacle ->
                if (obstacle is Obstacle.Block && obstacle.lives <= 0) return@filter false
                val blockHit = hitPoints.firstOrNull {
                    (it.x == obstacle.x && it.y in obstacle.y..obstacle.y + obstacle.height) ||
                            (it.x == obstacle.x + obstacle.width && it.y in obstacle.y..obstacle.y + obstacle.height) ||
                            (it.y == obstacle.y && it.x in obstacle.x..obstacle.x + obstacle.width) ||
                            (it.y == obstacle.y + obstacle.height && it.x in obstacle.x..obstacle.x + obstacle.width)
                }
                blockHit != null
            }

            if (hitCandidates.isEmpty()) continue

            for (obstacle in hitCandidates) { if (obstacle is Obstacle.Block) { obstacle.lives-- } }
            simulation += SimulationPoint(previousPoint, hitCandidates, i)
            totalDistance += i
            ballPosition = previousPoint

            val hitsX = hitCandidates.count { block -> hitPoints.any { it.x == block.x || it.x == block.x + block.width } }
            val hitsY = hitCandidates.count { block -> hitPoints.any { it.y == block.y || it.y == block.y + block.height } }

            ballAngle = when {
                hitsX == 1 && hitsY == 1 -> 180 - ballAngle // standard bounce from hitting or scratching corner of brick
                hitsX == 0 -> 180 - ballAngle               // standard hit from bottom / up
                hitsY == 0 -> -ballAngle                    // standard hit from left / right
                else -> -(180 - ballAngle)                  // hitting corner composed of 2 (or 3) blocks
            }

            break
        }
    } while (
        blocks.any { it.lives > 0 } &&
        simulation.last().hitBlock.none { it is Obstacle.Paddle } &&    // paddle hit
        simulation.last().position.y != screenHeight - ballHeight - 1   // bottom of the screen hit
    )

    if (simulation.last().hitBlock.any { it is Obstacle.Paddle }) {
        // TODO: adjust angle when paddle hit
        changeBallDirection(0)
    }

    val continueGame = simulation.last().hitBlock.any { it is Obstacle.Paddle }

    return SimulationMetadata(simulation, totalDistance, continueGame)
}

