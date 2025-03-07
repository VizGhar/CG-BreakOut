package com.codingame.game

import com.codingame.gameengine.core.AbstractPlayer
import com.codingame.gameengine.core.AbstractReferee
import com.codingame.gameengine.core.SoloGameManager
import com.codingame.gameengine.module.entities.GraphicEntityModule
import com.google.inject.Inject

//
class Referee : AbstractReferee() {

    @Inject
    private lateinit var gameManager: SoloGameManager<Player>

    @Inject
    private lateinit var graphicEntityModule: GraphicEntityModule

    override fun init() {
        gameManager.firstTurnMaxTime = 2000
        gameManager.turnMaxTime = 100
        gameManager.testCaseInput
        initVisual()
    }

    override fun gameTurn(turn: Int) {

        gameManager.winGame("Congrats!")
        return
        // input processing
        gameManager.player.sendInputLine("")

        try {
            // execution
            gameManager.player.execute()
            // processing outputs
            val output = gameManager.player.outputs[0].split(" ")

        } catch (e: AbstractPlayer.TimeoutException) {
            gameManager.loseGame("Timeout")
            return
        } catch (e: Exception) {
            e.printStackTrace()
            gameManager.loseGame("Invalid player output")
            return
        }

        gameManager.winGame("Congrats!")
    }

    private fun initVisual() {
        graphicEntityModule.background(flip = true, color = 0XFFFFFF)
        graphicEntityModule.game().add(
            *(0..<11).map { graphicEntityModule.brick(BrickHardness.MAX, BreakoutColor.RED).setY(0).setX(it * 128) }.toTypedArray(),
            *(0..<11).map { graphicEntityModule.brick(BrickHardness.MAX, BreakoutColor.GREY).setY(64).setX(it * 128) }.toTypedArray(),
            *(0..<11).map { graphicEntityModule.brick(BrickHardness.MAX, BreakoutColor.GREEN).setY(128).setX(it * 128) }.toTypedArray(),
            *(0..<11).map { graphicEntityModule.brick(BrickHardness.MAX, BreakoutColor.YELLOW).setY(192).setX(it * 128) }.toTypedArray(),
            *(0..<11).map { graphicEntityModule.brick(BrickHardness.MAX, BreakoutColor.ORANGE).setY(256).setX(it * 128) }.toTypedArray(),
            *(0..<11).map { graphicEntityModule.brick(BrickHardness.MAX, BreakoutColor.PURPLE).setY(320).setX(it * 128) }.toTypedArray(),
            *(0..<11).map { graphicEntityModule.brick(BrickHardness.MAX, BreakoutColor.RED).setY(384).setX(it * 128) }.toTypedArray(),
            *(0..<11).map { graphicEntityModule.brick(BrickHardness.MAX, BreakoutColor.GREY).setY(448).setX(it * 128) }.toTypedArray(),
            *(0..<11).map { graphicEntityModule.brick(BrickHardness.MAX, BreakoutColor.GREEN).setY(512).setX(it * 128) }.toTypedArray(),
        )
        graphicEntityModule.paddle(BreakoutColor.ORANGE)
        graphicEntityModule.ball(BreakoutColor.GREEN)
    }
}
