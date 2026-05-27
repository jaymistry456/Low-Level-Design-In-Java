package lowleveldesigns.snakesandladders;

import java.util.List;
import java.util.Map;
import java.util.Random;

/*
enums
* */
enum PlayerColor {
    RED,
    GREEN,
    BLUE,
    YELLOW;
}

/*
classes
* */
/*
Player
Dice
Board
SnakesAndLadders
* */

/*
Player
    knows:
        playerId
        name
        color
        position
    does:
        setPosition(int)
* */
class Player {
    private String playerId;
    private String name;
    private PlayerColor color;
    private int position;

    public Player (String playerId, String name, PlayerColor color) {
        this.playerId = playerId;
        this.name = name;
        this.color = color;
        this.position = 1;
    }

    public String getPlayerId () {
        return playerId;
    }

    public String getName () {
        return name;
    }

    public PlayerColor getColor () {
        return color;
    }

    public int getPosition () {
        return position;
    }

    public void setPosition (int position) {
        this.position = position;
    }
}

/*
Dice
    knows:
        sides (default 6)
    does:
        roll() -> int
* */
class Dice {
    private int sides;

    public Dice (int sides) {
        this.sides = sides;
    }

    public int roll () {
        Random rand = new Random();
        return rand.nextInt(sides) + 1;
    }
}

/*
Board
    knows:
        Map<Integer, Integer>   // Snakes head -> tail
        Map<Integer, Integer>   // Ladders bottom -> top
    does:
        getFinalPosition(int) -> int   // If a player lands on position X, Board should tell the final position
* */
class Board {
    private Map<Integer, Integer> snakes;
    private Map<Integer, Integer> ladders;

    public Board (Map<Integer, Integer> snakes, Map<Integer, Integer> ladders) {
        this.snakes = snakes;
        this.ladders = ladders;
    }

    public int getFinalPosition (int position) {
        // 1. Check Board out of bounds
        if(position > 100) {
            return -1;
        }

        // 2. Check Snakes
        if (snakes.containsKey(position)) {
            return snakes.get(position);
        }

        // 3. Check Ladders
        if (ladders.containsKey(position)) {
            return ladders.get(position);
        }

        return position;
    }
}

/*
SnakesAndLadders
    knows:
        List<Player>
        Dice
        Board
    does:
        startGame()
        playTurn(Player)
        checkWinner() -> Player
* */
public class SnakesAndLadders {
    private List<Player> players;
    private Dice dice;
    private Board board;

    public SnakesAndLadders (List<Player> players, Dice dice, Board board) {
        this.players = players;
        this.dice = dice;
        this.board = board;
    }

    public Player startGame () {
        while (true) {
            for (Player player: players) {
                // 1. Play turn
                playTurn(player);

                // 2. Check Winner after every turn
                Player winner = checkWinner();
                if (winner != null) {
                   return winner;
                }
            }
        }
    }

    public void playTurn (Player player) {
        // 1. Roll Dice
        int diceResult = dice.roll();
        int newPosition = player.getPosition() + diceResult;
        int finalPosition = board.getFinalPosition(newPosition);

        // 2. Set Position
        if (finalPosition != -1) {
            player.setPosition(finalPosition);
        }
    }

    public Player checkWinner () {
        for (Player player: players) {
            if (player.getPosition() == 100) {
                return player;
            }
        }
        return null;
    }
}

/*

SnakesAndLadders has-a List of Players
SnakesAndLadders has-a Dice
SnakesAndLadders has-a Board

* */