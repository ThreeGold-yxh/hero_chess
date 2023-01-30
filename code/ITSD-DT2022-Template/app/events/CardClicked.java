package events;


import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import myUtils.CardUtils;
import myUtils.TileUtils;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Tile;

import java.util.ArrayList;
import java.util.List;

/**
 * Indicates that the user has clicked an object on the game canvas, in this case a card.
 * The event returns the position in the player's hand the card resides within.
 * <p>
 * {
 * messageType = “cardClicked”
 * position = <hand index position [1-6]>
 * }
 *
 * @author Dr. Richard McCreadie
 */
public class CardClicked implements EventProcessor {

    @Override
    public void processEvent(ActorRef out, GameState gameState, JsonNode message) {


        //for tile, 0 is no colour, 1 is white highlight, 2 is red highlight
        //for card, BasicCommands.drawCard(out, hailstone_golem, 1, 0); the last bit is 0 for no highlight, the last bit is 1 for highlight

        //Not necessary anymore, because in the add method I have integrated the deletion of the previously highlighted
        //It is still necessary, because the current red highlight is also cleared when you click on the card.
        //to remove the currently highlighted white cells, as a new batch of cells will be highlighted, and to clear the gameState.currentWhiteHighLightTiles
        TileUtils.cancelTheCurrentWhiteHighLightTiles(out, gameState);

        // To remove the currently highlighted red cells, as a new batch of cells will be highlighted, and to clear out the gameState.currentRedHighLightTiles
        TileUtils.cancelTheCurrentRedHighLightTiles(out, gameState);


        //Then, get the position of the hand card
        //take care, handPosition start with 1!!!
        int handPosition = message.get("position").asInt();

        //fixme  debug
        System.out.println("你点击的这张牌的position是" + handPosition);

        //Get the card from the position
        Card card = CardUtils.getCardFromHandPosition(handPosition, gameState);



        //first of all, we should cancel the former highlight cards. Clear the map to avoid the multiple selection
        //Draw and highlight the card
        //we select this card and set it mapping to value handPosition in hand card Mode Map
        CardUtils.drawAndHighLightTheCard(out, card, handPosition, gameState);

        // If the current mana of this card is greater than the player's mana value, this card is highlighted, but the cell is not
        if (card.getManacost() > gameState.curPlayer.getMana()) {
            // simply unhighlight the previously highlighted cell
            TileUtils.cancelTheCurrentWhiteHighLightTiles(out, gameState);
            return;
        }


        // Get all available tiles when Clicking the card.
        List<Tile> availableTiles = CardUtils.getAllAvailableTilesWhenCalling(gameState, card);


        //Highlight all the available tiles
        TileUtils.whiteHighLightAvailableTiles(availableTiles, out, gameState);
    }

}
