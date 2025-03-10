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
        gameManager.maxTurns = remainingTurns
        ballAngle = gameManager.testCaseInput[3].toInt()
        blocks = gameManager.testCaseInput[4].split(";").map {
            val (id, color, lives) = it.split("-")
            Block(
                id = id.toInt(),
                color = BreakoutColor.valueOf(color),
                lives = lives.toInt()
            )
        }
        initVisual(
            BreakoutColor.valueOf(gameManager.testCaseInput[1]),
            BreakoutColor.valueOf(gameManager.testCaseInput[2])
        )
    }

    override fun gameTurn(turn: Int) {
        // remainingTurns
        gameManager.player.sendInputLine("$remainingTurns")
        gameManager.player.sendInputLine("${paddlePosition.x}")
        gameManager.player.sendInputLine("${ballPosition.x} $ballAngle")
        gameManager.player.sendInputLine("${blocks.size}")
        blocks.forEach { gameManager.player.sendInputLine("${it.id} ${it.lives} ${it.x} ${it.y}") }
        try {
            gameManager.player.execute()
            val output = gameManager.player.outputs[0].toInt()
            val sim = sim()
            graphicEntityModule.update(sim, output)
        } catch (e: AbstractPlayer.TimeoutException) {
            gameManager.loseGame("Timeout")
            return
        } catch (e: Exception) {
            e.printStackTrace()
            gameManager.loseGame("Invalid player output")
            return
        }
        if (turn == 2) {
            gameManager.winGame("Congrats!")
        }
    }

    private fun initVisual(paddleColor: BreakoutColor, ballColor: BreakoutColor) {
        graphicEntityModule.initGameBackground(flip = true, color = 0XFFFFFF)
        graphicEntityModule.initGameVisual(paddleColor, ballColor, tooltipModule)
    }
}
