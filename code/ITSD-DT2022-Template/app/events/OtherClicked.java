package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import myUtils.CardUtils;
import myUtils.TileUtils;
import structures.GameState;
import structures.basic.Unit;

import java.util.Map;

/**
 * Indicates that the user has clicked an object on the game canvas, in this case
 * somewhere that is not on a card tile or the end-turn button.
 * <p>
 * {
 * messageType = “otherClicked”
 * }
 *
 * @author Dr. Richard McCreadie
 */
public class OtherClicked implements EventProcessor {

    @Override
         public void processEvent(ActorRef out, GameState gameState, JsonNode message) {


        //first of all, we should cancel the former highlight. Clear the map to avoid the multiple selection
        CardUtils.cancelTheFormerHighLightCards(out, gameState);

        // to cancel the currently highlighted white cell, then empty the list
        TileUtils.cancelTheCurrentWhiteHighLightTiles(out, gameState);

        // to cancel the currently highlighted red cell, then empty the list
        TileUtils.cancelTheCurrentRedHighLightTiles(out, gameState);

        // To clear the current selected status of the unit
        for (Map.Entry<Unit, Integer> entry : gameState.curPlayer.unitsMode.entrySet()) {
            if (entry.getValue() == 1) {
                entry.setValue(0);
            }
        }


    }

}


