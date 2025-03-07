package com.codingame.game

import com.codingame.gameengine.module.entities.GraphicEntityModule
import com.codingame.gameengine.module.entities.Group

private const val blockWidth = 128
private const val widthInBlocks = 11
private const val boardWidth = blockWidth * widthInBlocks
private const val blockHeight = 64
private const val heightInBlocks = 16
private const val boardHeight = blockHeight * heightInBlocks

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

fun GraphicEntityModule.brick(hardness: BrickHardness, color: BreakoutColor): Group =
    breakoutSprite("brick_${hardness.value}", color)

fun GraphicEntityModule.paddle(color: BreakoutColor): Group =
    breakoutSprite("paddle", color).setY(world.height - 64).setX((world.width - 128) / 2)

fun GraphicEntityModule.ball(color: BreakoutColor): Group =
    breakoutSprite("ball", color).setY(world.height - 64 - 32).setX((world.width - 32) / 2)

fun GraphicEntityModule.game(): Group {
    val boardX = (world.width - boardWidth) / 2
    val boardY = (world.height - boardHeight) / 2

    createSprite().setImage("corner.png").setX(boardX - 32).setY(boardY - 32)
    createSprite().setImage("corner.png").setX(boardX + boardWidth).setY(boardY - 32)
    createTilingSprite().setImage("side.png").setX(boardX).setY(boardY - 32).setBaseWidth(boardWidth)
    createTilingSprite().setImage("side.png").setBaseHeight(32).setBaseWidth(world.height).setRotation(Math.toRadians(90.0)).setX(boardX).setY(boardY)
    createTilingSprite().setImage("side.png").setBaseHeight(32).setBaseWidth(world.height).setRotation(Math.toRadians(90.0)).setX(boardX + boardWidth + 32).setY(boardY)

    return createGroup().setY(boardY).setX(boardX)
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
