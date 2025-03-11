package com.codingame.game

import com.codingame.gameengine.core.AbstractPlayer
import com.codingame.gameengine.core.AbstractReferee
import com.codingame.gameengine.core.SoloGameManager
import com.codingame.gameengine.module.entities.GraphicEntityModule
import com.codingame.gameengine.module.tooltip.TooltipModule
import com.google.inject.Inject

class Referee : AbstractReferee() {

    @Inject
    private lateinit var gameManager: SoloGameManager<Player>

    @Inject
    private lateinit var graphicEntityModule: GraphicEntityModule

    @Inject
    private lateinit var tooltipModule: TooltipModule

    private var remainingTurns: Int = 0

    override fun init() {
        remainingTurns = gameManager.testCaseInput[0].toInt()
        gameManager.firstTurnMaxTime = 2000
        gameManager.turnMaxTime = 100
        gameManager.maxTurns = 200
        ballAngle = gameManager.testCaseInput[3].toInt()
        blocks = gameManager.testCaseInput[4].split(";").map {
            val (id, color, lives) = it.split("-")
            Obstacle.Block(
                id = id.toInt(),
                color = BreakoutColor.valueOf(color),
                lives = lives.toInt()
            )
        }
        graphicEntityModule.initGameBackground(flip = true, color = 0XFFFFFF)
        graphicEntityModule.initGameVisual(
            BreakoutColor.valueOf(gameManager.testCaseInput[1]),
            BreakoutColor.valueOf(gameManager.testCaseInput[2]), tooltipModule
        )
    }

    override fun gameTurn(turn: Int) {
        gameManager.player.sendInputLine("$remainingTurns")
        gameManager.player.sendInputLine("${paddlePosition.x}")
        gameManager.player.sendInputLine("${ballPosition.x} $ballAngle")
        gameManager.player.sendInputLine("${blocks.size}")
        blocks.forEach { gameManager.player.sendInputLine("${it.lives} ${it.x} ${it.y}") }
        try {
            gameManager.player.execute()
            val output = gameManager.player.outputs[0].toIntOrNull()
            if (output == null || output !in 0 ..< 640 - 64) {
                gameManager.loseGame("Moving paddle outside of game")
                return
            }
            paddlePosition = paddlePosition.copy(x = output)
            val simulation = simulateGame()
            graphicEntityModule.update(gameManager, simulation)
            if (blocks.all { it.lives <= 0 }) { gameManager.winGame("Congratulations"); return }
            if (!simulation.continueGame) { gameManager.loseGame("Ball escaped"); return }
        } catch (e: AbstractPlayer.TimeoutException) {
            gameManager.loseGame("Timeout")
            return
        } catch (e: Exception) {
            gameManager.loseGame("Invalid player output")
            return
        }
        if (remainingTurns-- == 0) {
            gameManager.loseGame("You didn't breakout in time")
        }
    }
}
