package com.codingame.game

import com.codingame.gameengine.module.entities.GraphicEntityModule
import com.codingame.gameengine.module.entities.Group

private const val blockWidth = 128
private const val widthInBlocks = 10
private const val blockHeight = 64
private const val heightInBlocks = 15

private const val boardWidth = blockWidth * widthInBlocks
private const val boardHeight = blockHeight * heightInBlocks

private val brickMap = mutableMapOf<Int, Group>()
private lateinit var paddleSprite: Group
private lateinit var ballSprite: Group

enum class BrickHardness(val value: Int) { MAX(3), MID(2), MIN(1) }

enum class BreakoutColor(val light: Int, val dark: Int) {
    GREY(0x99A09A, 0x606170),
    GREEN(0xC9E9BD, 0x507044),
    YELLOW(0xFFE9C4, 0xCAA05A),
    ORANGE(0xE9AF91, 0xAE6a47),
    RED(0xC27F87, 0x8B4049),
    PURPLE(0xB6A8AF, 0xb6A8AF)
}

private fun GraphicEntityModule.breakoutSprite(name: String, color: BreakoutColor): Group = createGroup().apply {
    add(createSprite().setImage("${name}_common.png"))
    add(createSprite().setImage("${name}_light.png").setTint(color.light))
    add(createSprite().setImage("${name}_dark.png").setTint(color.dark))
}

private fun GraphicEntityModule.brick(hardness: BrickHardness, color: BreakoutColor): Group =
    breakoutSprite("brick_${hardness.value}", color)

private fun GraphicEntityModule.paddle(color: BreakoutColor): Group =
    breakoutSprite("paddle", color).also { paddleSprite = it }

private fun GraphicEntityModule.ball(color: BreakoutColor): Group =
    breakoutSprite("ball", color).also { ballSprite = it }

fun update(sim: List<SimulationPoint>, output: Int) {
    val remove = brickMap.filter { (brickId, _) -> blocks.none { it.id == brickId } }
    for ((brickId, sprite) in remove) {
        sprite.setVisible(false)
        brickMap.remove(brickId)
    }

    if (output != -1) {
        paddleCenterPosition = paddleCenterPosition.copy(x = output)
    }

    paddleSprite
        .setY(boardHeight - (paddleCenterPosition.y + paddleHeight / 2) * 2)
        .setX((paddleCenterPosition.x - paddleWidth / 2) * 2)

    if (sim.size > 0) {
        ballSprite.setX(sim[0].position.x * 2).setY((boardHeight - sim[0].position.y * 2))
    } else {
        ballSprite
            .setY(boardHeight - (ballCenterPosition.y + ballHeight / 2) * 2)
            .setX((ballCenterPosition.x - ballWidth / 2) * 2)
    }
}

fun GraphicEntityModule.game(
    paddleColor: BreakoutColor,
    ballColor: BreakoutColor
): Group {
    val boardX = (world.width - boardWidth) / 2
    val boardY = world.height - boardHeight

    createSprite().setImage("corner.png").setX(boardX - 32).setY(boardY - 32)
    createSprite().setImage("corner.png").setX(boardX + boardWidth).setY(boardY - 32)
    createTilingSprite().setImage("side.png").setX(boardX).setY(boardY - 32).setBaseHeight(32).setBaseWidth(boardWidth)
    createTilingSprite().setImage("side.png").setBaseHeight(32).setBaseWidth(world.height).setRotation(Math.toRadians(90.0)).setX(boardX).setY(boardY)
    createTilingSprite().setImage("side.png").setBaseHeight(32).setBaseWidth(world.height).setRotation(Math.toRadians(90.0)).setX(boardX + boardWidth + 32).setY(boardY)

    return createGroup().setY(boardY).setX(boardX).apply {
        add(*blocks.map { obstacle -> brick(when(obstacle.lives) { 2 -> BrickHardness.MID; 3 -> BrickHardness.MAX; else -> BrickHardness.MIN}, obstacle.color).setY(boardHeight - obstacle.y * 2).setX(obstacle.x * 2).also { brickMap[obstacle.id] = it } }.toTypedArray())
        add(paddle(paddleColor))
        add(ball(ballColor))
        update(emptyList(), -1)
    }
}

fun GraphicEntityModule.background(
    color: Int = 0x23090B,
    flip: Boolean = false
): Group =
    createGroup().apply {
        // TODO: tint not working?
        add(createRectangle().setFillColor(0x000000).setWidth(world.width).setHeight(world.height))
        add(
        createTilingSprite().setBaseHeight(384).setBaseWidth(world.width).setTint(color).setImage("background.png").also {
            if (flip) it.setY(world.height).setScaleY(-1.0)
        })
    }
