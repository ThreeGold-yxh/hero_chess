package events;

import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import akka.actor.ActorRef;
import commands.BasicCommands;
import myUtils.*;
import structures.GameState;
import structures.basic.*;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

/**
 * Indicates that both the core game loop in the browser is starting, meaning
 * that it is ready to recieve commands from the back-end.
 * <p>
 * {
 * messageType = “initalize”
 * }
 *
 * @author Dr. Richard McCreadie
 */

public class Initalize implements EventProcessor{

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		// 		CommandDemo.executeDemo(out);
		// Modify the state of game to start.
		gameState.gameInitalised = true;

		// Initialize the board
		InitalUtils.initiateTheBoard(out, gameState);


		
		// Create both players and set avatars.
		gameState.humanPlayer = new Player();
		gameState.aiPlayer = new Player();
		
		//define current player and current enemy player
		//Let the human player take turn first
		gameState.curPlayer = gameState.humanPlayer;
		gameState.curEnemyPlayer = gameState.aiPlayer;
		gameState.turn = 1;
		
		// Update both players' mana each turn
		InitalUtils.updateManaEachTurn(out, gameState);
		
		// Set default health to both players.
		InitalUtils.setDefaultHealth(out, gameState);

		// draw the avatars on the canvas
		// the id of humanAvatar is 0.
		Unit unit1 = BasicObjectBuilders.loadUnit(StaticConfFiles.humanAvatar, 0, Unit.class);
		UnitUtils.setAvatar(unit1, "Human Avatar");
		unit1.setPositionByTile(gameState.tilesCollection[1][2]);
		gameState.humanPlayer.unitList.add(unit1);
		//set avatar_unit
		gameState.curPlayer.setAvatar_unit(unit1);

		// Back-end: create new object, setPosition, add to camp, set default life/attack/current life/current attack

		// Front-end: drawUnit
		BasicCommands.drawUnit(out, unit1, gameState.tilesCollection[1][2]);
		gameState.interval(50);
		BasicCommands.setUnitAttack(out, unit1, gameState.avatarAttack);
		gameState.interval(50);
		
		BasicCommands.setUnitHealth(out, unit1, gameState.curPlayer.defaultHealth);
		gameState.interval(50);
		
		// draw the avatars on the canvas
		// the id of aiAvatar is 21.
		Unit unit2 = BasicObjectBuilders.loadUnit(StaticConfFiles.aiAvatar, 21, Unit.class);
		UnitUtils.setAvatar(unit2, "AI Avatar");

		unit2.setPositionByTile(gameState.tilesCollection[7][2]);
		gameState.aiPlayer.unitList.add(unit2);

		//set avatar_unit
		gameState.curEnemyPlayer.setAvatar_unit(unit2);

		BasicCommands.drawUnit(out, unit2, gameState.tilesCollection[7][2]);
		gameState.interval(50);
		BasicCommands.setUnitAttack(out, unit2, gameState.avatarAttack);
		gameState.interval(50);
		BasicCommands.setUnitHealth(out, unit2, gameState.curPlayer.defaultHealth);
		gameState.interval(50);
		
		// Load the Players' card deck
		InitalUtils.loadPlayerCardDeck(gameState);	
		
		// Deal handcards to both sides
		InitalUtils.dealHandcardsToPlayer(out, gameState, 3);


	}


}




