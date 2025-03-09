package com.codingame.game

import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private const val maxAbsBallAngle = 70
private const val minAbsBallAngle = 10
const val paddleWidth = 64
const val paddleHeight = 32
const val ballWidth = 16
const val ballHeight = 16
var paddleCenterPosition = Position(640 / 2, 480 - 16)
var ballCenterPosition = Position(640 / 2, 480 - 32 - 8)
var ballAngle = minAbsBallAngle
var blocks = listOf<Block>()
private val gameBoard = Block(-1, BreakoutColor.RED, -1, 0, 0, 640, 480)

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
    val y: Int = (id / 10) * 32,
    val width: Int = 64,
    val height: Int = 32
) {
    val edges get() = listOf(
        Pair(x, y) to Pair(x + width, y),
        Pair(x, y) to Pair(x, y + height),
        Pair(x + width, y) to Pair(x + width, y + height),
        Pair(x, y + height) to Pair(x + width, y + height)
    )
}

data class SimulationPoint(val position: Position, val hitBlock: Block?)

fun sim(): List<SimulationPoint> {

    val result = mutableListOf<SimulationPoint>()
    val ballCorners = listOf(-8 to -8, 8 to -8, 8 to 8, -8 to 8)

    var x = ballCenterPosition.x
    var y = ballCenterPosition.y
    var angle = ballAngle

    while (true) {
        val rad = Math.toRadians(angle.toDouble())
        val dx = sin(rad)
        val dy = -cos(rad)
        var tMin = Double.POSITIVE_INFINITY
        var hit: Block? = null
        var hitPoint: Position? = null
        var reflectedAngle: Int? = null

        for (block in blocks + gameBoard) {
            if (block.lives == 0 || result.lastOrNull()?.hitBlock?.id?.let { it == block.id } == true) continue
            for ((ox, oy) in ballCorners) {
                val startX = x + ox
                val startY = y + oy

                for (edge in block.edges) {
                    val (x1, y1) = edge.first
                    val (x2, y2) = edge.second

                    val denom = (x2 - x1) * dy - (y2 - y1) * dx
                    val t = ((startX - x1) * dy - (startY - y1) * dx) / denom
                    val u = ((startX - x1) * (y2 - y1) - (startY - y1) * (x2 - x1)) / denom

                    if (t in 0.0..1.0 && u > 0 && u < tMin) {
                        tMin = u
                        hit = block
                        hitPoint = Position((startX + u * dx).roundToInt(), ceil(startY + u * dy).roundToInt())

                        reflectedAngle = when {
                            hitPoint.y == block.y || hitPoint.y == block.y + block.height -> 180 - ballAngle
                            hitPoint.x == block.x || hitPoint.x == block.x + block.width -> -ballAngle
                            else -> ballAngle
                        }
                    }
                }
            }
        }

        if (hit == null || hitPoint == null || reflectedAngle == null) break

        result += SimulationPoint(hitPoint, hit)
        hit.lives--
        ballCenterPosition = hitPoint
        ballAngle = reflectedAngle

        x = hitPoint.x
        y = hitPoint.y
        angle = reflectedAngle
    }
    return result
}

