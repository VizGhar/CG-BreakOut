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

    override fun init() {
        gameManager.firstTurnMaxTime = 2000
        gameManager.turnMaxTime = 100
        gameManager.maxTurns = 200
        ballAngle = gameManager.testCaseInput[2].toInt()
        blocks = gameManager.testCaseInput[3].split(";").map {
            val (id, color, lives) = it.split("-")
            Block(
                id = id.toInt(),
                color = BreakoutColor.valueOf(color),
                lives = lives.toInt()
            )
        }
        initVisual(
            BreakoutColor.valueOf(gameManager.testCaseInput[0]),
            BreakoutColor.valueOf(gameManager.testCaseInput[1])
        )
    }

    override fun gameTurn(turn: Int) {
        gameManager.player.sendInputLine("${ballCenterPosition.x} $ballAngle")
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
        graphicEntityModule.background(flip = true, color = 0XFFFFFF)
        graphicEntityModule.game(paddleColor, ballColor, tooltipModule)
    }
}
