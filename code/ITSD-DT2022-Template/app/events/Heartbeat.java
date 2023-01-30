package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import myUtils.TileUtils;
import myUtils.UnitUtils;
import structures.GameState;
import structures.basic.Player;
import structures.basic.Unit;

import java.util.Arrays;

/**
 * In the user’s browser, the game is running in an infinite loop, where there is around a 1 second delay
 * between each loop. Its during each loop that the UI acts on the commands that have been sent to it. A
 * heartbeat event is fired at the end of each loop iteration. As with all events this is received by the Game
 * Actor, which you can use to trigger game logic.
 * <p>
 * {
 * String messageType = “heartbeat”
 * }
 *
 * @author Dr. Richard McCreadie
 */
public class Heartbeat implements EventProcessor {

    @Override
    public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
        Player humanPlayer = gameState.humanPlayer;
        Player aiPlayer = gameState.aiPlayer;

        // The first thing is to determine the winner, whoever's blood is less than 0, the opponent wins
        if (humanPlayer.getHealth() <= 0) {
            BasicCommands.addPlayer1Notification(out, "AIPlayer Win", 20);
            System.out.println("AIPlayer Win");
            humanPlayer.setHealth(0);
            BasicCommands.setPlayer1Health(out, humanPlayer);
            try {
                Thread.sleep(100000000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (aiPlayer.getHealth() <= 0) {
            BasicCommands.addPlayer1Notification(out, "HumanPlayer Win", 20);
            System.out.println("HumanPlayer Win");
            aiPlayer.setHealth(0);
            BasicCommands.setPlayer2Health(out, aiPlayer);
            try {
                Thread.sleep(100000000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Then there is a blood correction, as the player/AI entity takes damage to synchronize its player's blood
        // Then if there is something like a Silver Shield Knight on the field, it will add attack power when the player takes damage
        // Is it a silver shield knight?
        //mana 3
        //attack 1
        //health 5
        //taunts: enemy units close to it are taunted by it, cannot move and can only attack it, and if a long-range unit attacks it, it will also be taunted
        //When your player incarnation takes damage, the Silver Shield Knight increases its attack power by 2 points.



        for (Unit unit : humanPlayer.unitList) {
            System.out.println(unit.getName());
        }



        for (Unit unit : aiPlayer.unitList) {
            System.out.println(unit.getName());
        }


        correctedPlayerHealthVolume(out, gameState, gameState.curPlayer);
        correctedPlayerHealthVolume(out, gameState, gameState.curEnemyPlayer);

        for (int i = 0; i < gameState.canvasLength; i++) {
            for (int j = 0; j < gameState.canvasWidth; j++) {
                if (TileUtils.isTileOccupiedByUnit(gameState.tilesCollection[j][i], gameState)) {
                    System.out.print("- ");
                } else {
                    System.out.print(". ");
                }
            }
            System.out.println("\n");
        }


    }


    public static void correctedPlayerHealthVolume(ActorRef out, GameState gameState, Player player) {

        // then blood levels are inconsistent, to modify blood levels

        if (player.getAvatar_unit().getCurHealth() < player.getHealth()) {
            player.setHealth(player.getAvatar_unit().getCurHealth());
            if (player == gameState.humanPlayer) {
                BasicCommands.setPlayer1Health(out, player);
            } else if (player == gameState.aiPlayer) {
                BasicCommands.setPlayer2Health(out, player);
            }

            //fixme
            if (player == gameState.humanPlayer) {
                System.out.println("human player unit list size = " + player.unitList.size());
                for (Unit unit : player.unitList) {
                    System.out.println(unit.getName());
                }
            } else if (player == gameState.aiPlayer) {
                System.out.println("ai player unit list size = " + player.unitList.size());
                for (Unit unit : player.unitList) {
                    System.out.println(unit.getName());
                }
            }

            // Here the attack power of the Silver Shield Knight is to be increased
            for (Unit unit : player.unitList) {

                System.out.println("check Silverguard Knight" + unit.getName());
                if (unit.getName().equals("Silverguard Knight")) {

                    unit.setCurAttack(unit.getCurAttack() + 2);

                    // Front-end display
                    BasicCommands.setUnitAttack(out, unit, unit.getCurAttack());
                    gameState.interval(200);
                    BasicCommands.setUnitHealth(out, unit, unit.getCurHealth());
                    gameState.interval(200);
                }
            }
        } else if (player.getAvatar_unit().getCurHealth() > player.getHealth()) {
            player.setHealth(player.getAvatar_unit().getCurHealth());
            BasicCommands.setPlayer1Health(out, player);
        }

    }

}
