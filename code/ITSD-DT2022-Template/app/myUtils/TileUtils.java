package myUtils;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TileUtils {

    /**
     * Check if a cell is out of bounds
     */
    public static boolean isOutOfBorder(int tilex, int tiley,  GameState gameState){
        return  tilex < 0 || tilex >= gameState.canvasWidth || tiley < 0 || tiley >= gameState.canvasLength;
    }

    /**
     * Determine if the current tile is occupied by unit
     * @param tile tile
     * @param gameState  gameState
     * @return
     */
    public static boolean isTileOccupiedByUnit(Tile tile, GameState gameState){
        return isTileOccupiedByCurPlayerUnit(tile, gameState) || isTileOccupiedByCurEnemyUnit(tile, gameState);
    }

    /**
     * Determine if the tile is occupied by the current player's unit
     * @param tile  current tile
     * @param gameState  gameState
     * @return
     */
    public static boolean isTileOccupiedByCurPlayerUnit(Tile tile, GameState gameState){
        for (Unit unit : gameState.curPlayer.unitList) {
            //If you find a unit whose position matches the position of the entered tile, that means it is it, add res
            if (unit.getPosition().getTilex() == tile.getTilex()
                    && unit.getPosition().getTiley() == tile.getTiley()){
                return true;
            }
        }
        return false;
    }

    /**
     * Determine if the tile is occupied by the current enemy unit
     * @param tile current tile
     * @param gameState
     * @return
     */
    public static boolean isTileOccupiedByCurEnemyUnit(Tile tile, GameState gameState){
        for (Unit unit : gameState.curEnemyPlayer.unitList) {
            //If you find a unit whose position matches the position of the entered tile, that means it is it, add res
            if (unit.getPosition().getTilex() == tile.getTilex()
                    && unit.getPosition().getTiley() == tile.getTiley()){
                return true;
            }
        }
        return false;
    }

    /**
     * Get a tile by x,y coordinates, if not it is null
     * @param tilex  x coordinate
     * @param tiley  y-coordinate
     * @param gameState  Game Status
     * @return
     */
    public static Tile getTileFromPosition(int tilex, int tiley, GameState gameState){
        //如果出界返回null
        if (isOutOfBorder(tilex, tiley, gameState)){
            return null;
        }
        //否则返回这个tile对象
        return gameState.tilesCollection[tilex][tiley];
    }

    /**
     * Based on a unit object, get the cell tile it occupies, if the unit is not yet on, then it is null
     * @param unit  Objects of the Legion
     * @param gameState  Game Status
     * @return
     */
    public static Tile getTileFromUnit(Unit unit, GameState gameState){
        int tilex = unit.getPosition().getTilex();
        int tiley = unit.getPosition().getTiley();
        // Determine if the boundary is crossed, if it is, return null
        if (TileUtils.isOutOfBorder(tilex, tiley, gameState)){
            return null;
        }
        return TileUtils.getTileFromPosition(tilex, tiley, gameState);
    }


    public static List<Tile> getSorroundingEightTiles(Tile tile, GameState gameState){
        List<Tile> res = new ArrayList<>();
        String[] directions = new String[]{
                "left-up",
                "up",
                "right-up",
                "right",
                "right-down",
                "down",
                "left-down",
                "left"
        };

        // If the tile itself is out of bounds.
        if (TileUtils.isOutOfBorder(tile.getTilex(), tile.getTiley(), gameState)){
            return res;
        }


        for (String direction : directions) {
            int tilex = tile.getTilex();
            int tiley = tile.getTiley();
            int testtilex = tilex;
            int testtiley = tiley;

            if ("left-up".equals(direction)){
                tilex = tile.getTilex() - 1;
                tiley = tile.getTiley() + 1;
            }
            else if ("up".equals(direction)){
                tiley = tile.getTiley() + 1;
            }
            else if ("right-up".equals(direction)){
                tilex = tile.getTilex() + 1;
                tiley = tile.getTiley() + 1;
            }
            else if ("right".equals(direction)){
                tilex = tile.getTilex() + 1;
            }
            else if ("right-down".equals(direction)){
                tilex = tile.getTilex() + 1;
                tiley = tile.getTiley() - 1;
            }
            else if ("down".equals(direction)){
                tiley = tile.getTiley() - 1;
            }
            else if ("left-down".equals(direction)){
                tilex = tile.getTilex() - 1;
                tiley = tile.getTiley() - 1;
            }
            else if ("left".equals(direction)){
                tilex = tile.getTilex() - 1;
            }

            //If out of bounds, continue
            if (TileUtils.isOutOfBorder(tilex, tiley, gameState)){
                continue;
            }

            Tile newTile = TileUtils.getTileFromPosition(tilex, tiley, gameState);
            res.add(newTile);
        }
        return res;
    }

    public static List<Tile> getSorroundingFourTiles(Tile tile, GameState gameState){
        List<Tile> res = new ArrayList<>();
        String[] directions = new String[]{
                "up",
                "right",
                "down",
                "left"
        };

        // If the tile itself is out of bounds.
        if (TileUtils.isOutOfBorder(tile.getTilex(), tile.getTiley(), gameState)){
            return res;
        }

//        System.err.println("[" + tile.getTilex() +", " + tile.getTiley() + "]" + "是坐标点格子");
        for (String direction : directions) {
            int tilex = tile.getTilex();
            int tiley = tile.getTiley();
            int testtilex = tilex;
            int testtiley = tiley;

            if ("up".equals(direction)){
                tiley = tile.getTiley() + 1;
            }
            else if ("right".equals(direction)){
                tilex = tile.getTilex() + 1;
            }
            else if ("down".equals(direction)){
                tiley = tile.getTiley() - 1;
            }
            else if ("left".equals(direction)){
                tilex = tile.getTilex() - 1;
            }

            //If out of bounds, continue
            if (TileUtils.isOutOfBorder(tilex, tiley, gameState)){
//                System.err.println("[" + tilex +", " + tiley + "]" + "出界了，不允许添加进surroundings");
                continue;
            }

            // not out of bounds, add in

            Tile newTile = TileUtils.getTileFromPosition(tilex, tiley, gameState);
            res.add(newTile);
        }
        return res;
    }



    /**
     * White highlight all the tiles in the list and add them to gameState.currentWhiteHighLightTiles
     * @param availableTiles  Highlightable tiles list, optionally available from CardUtils' tool method
     * @param out   Output stream
     * @param gameState     Status category
     */
    public static void whiteHighLightAvailableTiles(List<Tile> availableTiles, ActorRef out, GameState gameState){
        // To remove the current highlighted white cells, as a new batch of cells will be highlighted, and to clear the gameState.currentWhiteHighLightTiles
		TileUtils.cancelTheCurrentWhiteHighLightTiles(out, gameState);
		if (gameState.curPlayer == gameState.humanPlayer){
            if (availableTiles != null) {
                for (Tile availableTile : availableTiles) {
                    //highlight the available tiles
                    BasicCommands.drawTile(out, availableTile, 1);
                    gameState.interval(20);
                }
            }
        }

        // The current white highlighted tiles should be stored in the List<Tile> currentWhiteHighLightTiles of the GameState class to make it easier to clear them or check their status later.
        if (availableTiles != null) {
            gameState.currentWhiteHighLightTiles.addAll(availableTiles);
        }
//        for (Tile availableTile : availableTiles) {
//            gameState.tilesMode.put(availableTile, 1);
//        }

    }


    /**
     * Highlight all the tiles in the list in red and add them to gameState.currentRedHighLightTiles
     * @param availableTiles  Highlightable tiles list, optionally available from CardUtils' tool method
     * @param out  Output stream
     * @param gameState  Status category
     */
    public static void redHighLightAvailableTiles(List<Tile> availableTiles, ActorRef out, GameState gameState){
        // First of all, all currently highlighted red cells should be cancelled, as a new batch of cells will be highlighted, and the gameState.currentRedHighLightTiles should be cleared.
        cancelTheCurrentRedHighLightTiles(out, gameState);
        if (gameState.curPlayer == gameState.humanPlayer){
            if (availableTiles != null) {
                for (Tile availableTile : availableTiles) {
                    //red highlight the available tiles
                    BasicCommands.drawTile(out, availableTile, 2);
                    gameState.interval(20);
                }
            }
        }

        // The current red highlighted tiles should be stored in the List<Tile> currentRedHighLightTiles of the GameState class to make it easier to clear them or check their status later.
        if (availableTiles != null) {
            gameState.currentRedHighLightTiles.addAll(availableTiles);
        }
//        for (Tile availableTile : availableTiles) {
//            gameState.tilesMode.put(availableTile, 2);
//        }
    }

    /**
     * Remove all currently highlighted white cells
     * @param out output stream
     * @param gameState game state parameter
     */
    public static void cancelTheCurrentWhiteHighLightTiles(ActorRef out, GameState gameState){
        // To remove all currently highlighted white cells, as a new batch of cells will be highlighted
        for (Tile currentWhiteHighLightTile : gameState.currentWhiteHighLightTiles) {
            BasicCommands.drawTile(out, currentWhiteHighLightTile, 0);
            gameState.interval(20);
        }
        // then empty the list
        gameState.currentWhiteHighLightTiles.clear();
//        gameState.tilesMode.clear();
    }


    public static void refreshTheCurrentWhiteHighLightTilesNotShow(GameState gameState, List<Tile> newCurrentWhiteHighLightTilesNotShow){
        // Empty the original
        gameState.currentWhiteHighLightTilesNotShow.clear();
        // then update the set of white highlighted cells that are not displayed
        gameState.currentWhiteHighLightTilesNotShow.addAll(newCurrentWhiteHighLightTilesNotShow);
    }


    /**
     *  把当前所有高亮的红色单元格取消高亮
     * @param out  output stream
     * @param gameState  game state parameter
     */
    public static void cancelTheCurrentRedHighLightTiles(ActorRef out, GameState gameState){
        // To remove the currently highlighted red cells, as a new batch of cells will be highlighted
        for (Tile currentRedHighLightTile : gameState.currentRedHighLightTiles) {
            BasicCommands.drawTile(out, currentRedHighLightTile, 0);
            gameState.interval(20);
        }
        // then empty the list
        gameState.currentRedHighLightTiles.clear();
//        gameState.tilesMode.clear();
    }

    /**
     * 得到两个tile之间的绝对距离，用平方和表示
     * @param tileA
     * @param tileB
     * @return
     */
    public static int getAbsoluteDistanceFromTiles(Tile tileA, Tile tileB){
        return (tileA.getTilex() - tileB.getTilex()) * (tileA.getTilex() - tileB.getTilex())
                + (tileA.getTiley() - tileB.getTiley()) * (tileA.getTiley() - tileB.getTiley());
    }


    public static void otherClick(ActorRef out, GameState gameState) {
        System.out.println("otherclick executes!");
        // Handled as anotherclick event
        // To unhighlight the card selected in the previous turn
        //first of all, we should cancel the former highlight. Clear the map to avoid the multiple selection
        CardUtils.cancelTheFormerHighLightCards(out, gameState);

        // to cancel the currently highlighted white cell, then empty the list
        cancelTheCurrentWhiteHighLightTiles(out, gameState);

        // to cancel the currently highlighted red cell, then empty the list
        TileUtils.cancelTheCurrentRedHighLightTiles(out, gameState);
        // Clear the selected status of unitsMode
        for (Map.Entry<Unit, Integer> entry : gameState.curPlayer.unitsMode.entrySet()) {
            if (entry.getValue() == 1) {
                entry.setValue(0);
            }
        }
    }

    /**
     * Find the relative distance between two cells, i.e. the distance to move horizontally and vertically over
     * @param tileA
     * @param tileB
     * @return
     */
    public static int getRelativeDistanceFormTiles(Tile tileA, Tile tileB){
        return Math.abs(tileA.getTilex() - tileB.getTilex()) + Math.abs(tileA.getTiley() - tileB.getTiley());
    }

}
