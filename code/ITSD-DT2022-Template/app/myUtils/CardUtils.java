package myUtils;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.*;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CardUtils {
    // Up to six cards in one hand, numbered positions 1 to 6
    //Spell card
    private static String[] spellCards = {
            "Sundrop Elixir",
            "Truestrike",
            "Entropic Decay",
            "Staff of Y'Kir'"
    };

    //get a card from its hand card position
    // get a reference to that card from the hand position returned to you from the front end
    public static Card getCardFromHandPosition(int handPosition, GameState gameState) {
        //There should be a handPosition - 1
        return gameState.curPlayer.handcardList.get(handPosition - 1);
    }

    /**
     * Add a card from the library to the current player (possibly the AI)
     *
     * @param out
     * @param gameState
     * @param position  is this card you want to add, in the front hand 1 - 6
     */
    public static void addCardToCurPlayer(ActorRef out, GameState gameState, int position) {
        //Do nothing if the library is empty
        if (gameState.curPlayer.deck.size() == 0) {
            // If the current character is a human player, it alerts the human player that the library is empty,
            // if it's the AI, it does nothing because the AI has no prompt command
            if (gameState.curPlayer == gameState.humanPlayer) {
                BasicCommands.addPlayer1Notification(out, "Your card Deck is empty", 1);
            }
            return;
        }
        // Take cards from the library
        Card card = gameState.curPlayer.deck.poll();
        //Add to the player's hand
        gameState.curPlayer.handcardList.add(card);
        //Maintain this card in relation to the hand showing position
        gameState.curPlayer.handcardPositionMap.put(card, position);
        // It is the human player who has to draw this card
        if (gameState.curPlayer == gameState.humanPlayer) {
            BasicCommands.drawCard(out, card, position, 0);
            gameState.interval(20);
        }
    }


    /**
     * Delete a card from the player's (AI's) hand that should have been played, and move all cards to the right left
     *
     * @param out
     * @param gameState
     * @param card      Deleted Cards
     * @return
     */
    public static boolean deleteCardFromCurPlayer(ActorRef out, GameState gameState, Card card) {
        // deleteCard

        int position = CardUtils.getPositionOfCard(gameState.curPlayer, card);


        // If -1, the card was not found and deletion failed
        if (position == -1) {
            return false;
        }
        // Now to find all the cards to the right of the deleted card
        List<Card> rightSideCards = new ArrayList<>();
        for (int i = position; i < gameState.curPlayer.handcardList.size(); i++) {
            rightSideCards.add(gameState.curPlayer.handcardList.get(i));
        }



        // Human has to delete this card in the front end
        if (gameState.curPlayer == gameState.humanPlayer) {

            BasicCommands.addPlayer1Notification(out, "deleteCard", 2);
            BasicCommands.deleteCard(out, position);
        }

        // Delete the current card in list and map
        System.out.println("在后端中删除这张卡");
        gameState.curPlayer.handcardList.remove(card);
        gameState.curPlayer.handcardPositionMap.remove(card);
        // Don't forget to handle it in handcardMode as well. Avoid drawing this card out again in the unhighlight function later
        gameState.curPlayer.handcardMode.remove(card);

        // All cards on the right are to be moved one space to the left
        if (rightSideCards.size() != 0) {
            for (Card rightSideCard : rightSideCards) {
                // A human being must delete this card at the front end first
                if (gameState.curPlayer == gameState.humanPlayer) {
                    BasicCommands.deleteCard(out, gameState.curPlayer.handcardPositionMap.get(rightSideCard));
                    gameState.interval(20);
                }
                //then update the map
                gameState.curPlayer.handcardPositionMap.put(rightSideCard, gameState.curPlayer.handcardPositionMap.get(rightSideCard) - 1);
                // If you are a human, draw this card in the front left frame
                if (gameState.curPlayer == gameState.humanPlayer) {
                    BasicCommands.drawCard(out, rightSideCard, gameState.curPlayer.handcardPositionMap.get(rightSideCard), 0);
                    gameState.interval(20);

                }
            }
        }
        return true;
    }

    /**
     * Get the position of this card in the hand (1-6) according to the player object and card, if not in the hand, return -1
     *
     * @param player
     * @param card
     * @return
     */
    public static int getPositionOfCard(Player player, Card card) {
        // If the card is not in the current hand map, return -1 directly
        if (!player.handcardPositionMap.containsKey(card)) {
            return -1;
        }
        return player.handcardPositionMap.get(card);
    }

    /**
     * Highlight the selected card and adjust the current player's handcardMode, which is the map of the selected card (comes with the ability to unhighlight previously highlighted cards)
     *
     * @param out
     * @param card
     * @param handPosition The position of the card in the hand, which can be obtained directly from the front end int handPosition = message.get("position").asInt();
     * @param gameState
     */
    public static void drawAndHighLightTheCard(ActorRef out, Card card, int handPosition, GameState gameState) {
        // First you should unhighlight the previously selected card, because you have to highlight the new card, call the tool method
        cancelTheFormerHighLightCards(out, gameState);
        //Draw and highlight the card
        if (gameState.curPlayer == gameState.humanPlayer) {
	        BasicCommands.drawCard(out, card, handPosition, 1);
	        gameState.interval(20);
        }
        //we select this card and set it mapping to value handPosition in hand card Mode Map
        gameState.curPlayer.handcardMode.put(card, handPosition);
    }

    /**
     * Remove the previously highlighted card and clear humanPlayer.handcardMode
     *
     * @param out
     * @param gameState
     */
    public static void cancelTheFormerHighLightCards(ActorRef out, GameState gameState) {
        ///for tile, 0 is no colour, 1 is white highlight, 2 is red highlight
        //for card, BasicCommands.drawCard(out, hailstone_golem, 1, 0); last digit is 0 for no highlight, last digit is 1 for highlight

        // First, unhighlight the previously selected card, as it is time to highlight the new one
        //first of all, we should cancel the former highlight cards. Clear the map to avoid the multiple selection
        for (Map.Entry<Card, Integer> entry : gameState.curPlayer.handcardMode.entrySet()) {
            Card card = entry.getKey();
            int mapHandPosition = entry.getValue();
            if ((mapHandPosition != 0) && (gameState.curPlayer == gameState.humanPlayer)) {
                BasicCommands.drawCard(out, card, mapHandPosition, 0);
                gameState.interval(20);
            }
        }
        gameState.curPlayer.handcardMode.clear();
    }


    // Is it a Legion card
    public static boolean isUnitCard(Card card) {
        return !isSpellCard(card);
    }

    // Is it a spell card
    public static boolean isSpellCard(Card card) {
        for (String spellCard : spellCards) {
            if (spellCard.equals(card.getCardname())) {
                return true;
            }
        }
        return false;
    }

    //Determine if it is a true damage card Deal two points of damage to the enemy Truestrike
    public static boolean isTruestrike(Card card) {
        return "Truestrike".equals(card.getCardname());
    }

    //Determine if a Sunset Potion card restores 5 points of blood to a friendly party Sundrop Elixir
    public static boolean isSundrop_Elixir(Card card) {
        return "Sundrop Elixir".equals(card.getCardname());
    }

    //Determine if the card is an attack adder, add 2 attack points to the player's incarnation
    public static boolean isStaff_of_Y_Kir(Card card) {
        return "Staff of Y'Kir'".equals(card.getCardname());
    }

    // determine if it is entropy reduction, spike an enemy unit, cannot be an incarnation of an enemy
    public static boolean isEntropic_Decay(Card card) {
        return "Entropic Decay".equals(card.getCardname());
    }

    public static void useSpellCard(ActorRef out, GameState gameState, Card card, Unit unit) {
        String[] effects = {
                //add attack
                StaticConfFiles.f1_buff,
                //true strike
                StaticConfFiles.f1_inmolation,
                //death directly spell
                StaticConfFiles.f1_martyrdom,
                //add health
                StaticConfFiles.f1_summon
        };


        if (isTruestrike(card)) {
            EffectAnimation ef = BasicObjectBuilders.loadEffect(effects[1]);
            useTruestrike(unit);
            BasicCommands.playEffectAnimation(out, ef, TileUtils.getTileFromUnit(unit, gameState));
        } else if (isSundrop_Elixir(card)) {
            EffectAnimation ef = BasicObjectBuilders.loadEffect(effects[2]);
            useSundrop_Elixir(unit);
            BasicCommands.playEffectAnimation(out, ef, TileUtils.getTileFromUnit(unit, gameState));
        } else if (isStaff_of_Y_Kir(card)) {
            EffectAnimation ef = BasicObjectBuilders.loadEffect(effects[0]);
            useStaff_of_Y_Kir(unit);
            BasicCommands.playEffectAnimation(out, ef, TileUtils.getTileFromUnit(unit, gameState));
        } else if (isEntropic_Decay(card)) {
            EffectAnimation ef = BasicObjectBuilders.loadEffect(effects[3]);
            useEntropic_Decay(unit);
            BasicCommands.playEffectAnimation(out, ef, TileUtils.getTileFromUnit(unit, gameState));
        }
    }

    // Use the True Damage card Deal two points of damage to the enemy Truestrike
    public static void useTruestrike(Unit unit) {
        unit.setCurHealth(unit.getCurHealth() - 2);
    }

    // Use Sunset Potion card to restore 5 points of blood to a friendly party Sundrop Elixir
    public static void useSundrop_Elixir(Unit unit) {
        // 不能超过默认生命值
        unit.setCurHealth(unit.getCurHealth() <= unit.getDefaultHealth() - 5 ? unit.getCurHealth() + 5 : unit.getDefaultHealth());
    }

    // Add 2 points of attack to the player's avatar by using cards that add attack
    public static void useStaff_of_Y_Kir(Unit unit) {
        unit.setCurAttack(unit.getCurAttack() + 2);
    }

    // Use entropy reduction to kill an enemy unit in seconds, cannot be an incarnation of the enemy
    public static void useEntropic_Decay(Unit unit) {
        unit.setCurHealth(0);
    }


    // Get their tiles directly based on a set of units
    public static List<Tile> getTilesFormUnits(GameState gameState, List<Unit> unitList) {
        List<Tile> res = new ArrayList<>();
        for (Unit unit : unitList) {
            int tilex = unit.getPosition().getTilex();
            int tiley = unit.getPosition().getTiley();
            // once it's not out of border
            if (!TileUtils.isOutOfBorder(tilex, tiley, gameState)) {
                res.add(gameState.tilesCollection[tilex][tiley]);
            }
        }
        return res;
    }

    //get all occupied tiles which is occupied by current player's units
    public static List<Tile> getAllOccupiedTilesFromCurPlayer(GameState gameState) {
        // get the tile occupied by all the units of the current player
        return getTilesFormUnits(gameState, gameState.curPlayer.unitList);
    }

	// Get all available tiles when Clicking the card.
    public static List<Tile> getAllAvailableTilesWhenCalling(GameState gameState, Card card) {
    	List<Tile> availableTiles = new ArrayList<>();
        // Is it a Legion card
    	if (CardUtils.isUnitCard(card)){
    		availableTiles = CardUtils.getAllAvailableTilesWhenCallingNewUnit(gameState, card);
    	}
        // Is it real damage
    	else if (CardUtils.isTruestrike(card)){
    		availableTiles = CardUtils.getAvailableTilesWhenClicking_truestrike(gameState);
    	}
        // Is it a sunset potion
    	else if (CardUtils.isSundrop_Elixir(card)){
    		availableTiles = CardUtils.getAvailableTilesWhenClicking_sundrop_elixir(gameState);
    	}
        // Is it a plus attack
    	else if (CardUtils.isStaff_of_Y_Kir(card)){
    		availableTiles = CardUtils.getAvailableTilesWhenClicking_staff_of_ykir(gameState);
    	}
        // Is not entropy decreasing
    	else if (CardUtils.isEntropic_Decay(card)){
    		availableTiles = CardUtils.getAvailableTilesWhenClicking_entropic_decay(gameState);
    	}
		return availableTiles; 	
    }

    // get the range of cells that should be highlighted when the legion card is currently selected by the player, i.e. the range that can be summoned
    //get all available tiles which should be highlight when calling new unit
    public static List<Tile> getAllAvailableTilesWhenCallingNewUnit(GameState gameState, Card card) {
        List<Tile> res = new ArrayList<>();
        // If it is a unit that can be called anywhere, return List<Tile> to return all unoccupied Tiles
        if (UnitUtils.unitEffectSummonAnywhere(card)) {
            for (int x = 0; x < gameState.canvasWidth; x++) {
                for (int y = 0; y < gameState.canvasLength; y++) {
                    if (!TileUtils.isOutOfBorder(x, y, gameState) && !TileUtils.isTileOccupiedByUnit(gameState.tilesCollection[x][y], gameState)) {
                        res.add(gameState.tilesCollection[x][y]);
                    }
                }
            }
            return res;
        }
        // otherwise traverse a circle around the current friendly unit normally
        List<Tile> occupiedTiles = CardUtils.getAllOccupiedTilesFromCurPlayer(gameState);
        for (Tile occupiedTile : occupiedTiles) {
            int tilex = occupiedTile.getTilex();
            int tiley = occupiedTile.getTiley();
            // Scan the current cell in a circle, not out of bounds, and not occupied by another unit
            //Scan the current tile in a circle
            for (int x = tilex - 1; x <= tilex + 1; x++) {
                for (int y = tiley - 1; y <= tiley + 1; y++) {
                    if (!TileUtils.isOutOfBorder(x, y, gameState) && !TileUtils.isTileOccupiedByUnit(gameState.tilesCollection[x][y], gameState)) {
                        res.add(gameState.tilesCollection[x][y]);
                    }
                }
            }

        }
        return res;
    }

    //real damage
    //deal 2 damage to an enemy unit
    // Range of action, all enemy units
    public static List<Tile> getAvailableTilesWhenClicking_truestrike(GameState gameState) {
        return getTilesFormUnits(gameState, gameState.curEnemyPlayer.unitList);
    }

    //Sunset Potion
    //add 5 health to a unit, can't over its starting health value
    // Range of action, all own-party units
    public static List<Tile> getAvailableTilesWhenClicking_sundrop_elixir(GameState gameState) {
        return getTilesFormUnits(gameState, gameState.curPlayer.unitList);
    }

    //Add attack, add 2 points of attack to your own incarnation UNIT
    //Action range: your own avatar
    public static List<Tile> getAvailableTilesWhenClicking_staff_of_ykir(GameState gameState) {
        List<Tile> res = new ArrayList<>();
        for (Unit unit : gameState.curPlayer.unitList) {
            //when find the avatar unit
            if (unit == gameState.curPlayer.getAvatar_unit()) {
                int tilex = unit.getPosition().getTilex();
                int tiley = unit.getPosition().getTiley();
                // once it's not out of border
                if (!TileUtils.isOutOfBorder(tilex, tiley, gameState)) {
                    res.add(gameState.tilesCollection[tilex][tiley]);
                }
            }
        }
        return res;
    }

    // Entropic Decay  熵减
    // Reduce a non-avatar unit to 0 health 把一支非化身的军团血量减少为0，就是秒杀一个单位
    // Range of action: all enemyunits except enemy incarnations //
    public static List<Tile> getAvailableTilesWhenClicking_entropic_decay(GameState gameState) {
        List<Tile> res = new ArrayList<>();
        for (Unit unit : gameState.curEnemyPlayer.unitList) {
            //when find the avatar unit
            if (!(unit == gameState.curEnemyPlayer.getAvatar_unit())) {
                int tilex = unit.getPosition().getTilex();
                int tiley = unit.getPosition().getTiley();
                // once it's not out of border
                if (!TileUtils.isOutOfBorder(tilex, tiley, gameState)) {
                    res.add(gameState.tilesCollection[tilex][tiley]);
                }
            }
        }
        return res;

    }


}
