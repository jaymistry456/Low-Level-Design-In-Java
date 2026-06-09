package lowleveldesigns.tictactoe;

/*
enums
* */
enum GameStatus {
    NOT_STARTED,
    IN_PROGRESS,
    WON,
    DRAW;
}

enum Mark {
    X,
    O;
}

/*
classes
* */
/*
Position
Player
Board
TicTacToe
* */

/*
Position
* */
record Position(int row, int col) {}

/*
Player
    knows:
        playerId
        name
        Mark
    does:
        nothing (data carrier)
* */
class Player {
    private String playerId;
    private String name;
    private Mark mark;

    public Player(String playerId, String name, Mark mark) {
        this.playerId = playerId;
        this.name = name;
        this.mark = mark;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getName() {
        return name;
    }

    public Mark getMark() {
        return mark;
    }
}

/*
Board
    knows:
        Mark[][] board
    does:
        isMoveValid(Position, Mark) -> boolean
        markPosition(Position, Mark)
        checkWinner() -> Mark
        isBoardFull() -> boolean
* */
class Board {
    private Mark[][] board;

    public Board() {
        this.board = new Mark[3][3];
    }

    public boolean isMoveValid(Position position) {
        return board[position.row()][position.col()] == null;
    }

    public void markPosition(Position position, Mark mark) {
        if(!isMoveValid(position)) return;
        board[position.row()][position.col()] = mark;
    }

    public Mark checkWinner() {
        Mark winner = null;

        // Rows
        for(int i = 0; i < board.length; i++) {
            winner = board[i][0];
            if(winner == null) continue;;
            for(int j = 0; j < board[0].length; j++) {
                if(board[i][j] != winner) {
                    winner = null;
                    break;
                }
            }
            if(winner != null) return winner;
        }

        // Cols
        for(int j = 0; j < board[0].length; j++) {
            winner = board[0][j];
            if(winner == null) continue;;
            for(int i = 0; i < board.length; i++) {
                if(board[i][j] != winner) {
                    winner = null;
                    break;
                }
            }
            if(winner != null) return winner;
        }

        // Diagonals
        for(int i = 0; i < board.length; i++) {
            winner = board[0][0];
            if(winner == null) continue;;
            if(board[i][i] != winner) {
                winner = null;
                break;
            }
            if(winner != null) return winner;
        }
        int i = board.length - 1;
        int j = 0;
        winner = board[i][j];
        if(winner == null) return null;;
        for(int k = 0; k < board.length; k++) {
            if(board[i][j] != winner) {
                winner = null;
                break;
            }
            i--;
            j++;
        }

        return winner;
    }

    public boolean isBoardFull() {
        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board[0].length; j++) {
                if(board[i][j] == null) return false;
            }
        }
        return true;
    }
}

/*
TicTacToe
    knows:
        Player player1
        Player player2
        Player currPlayer
        Board
        GameStatus
    does:
        startGame() -> Player
        playTurn(Player, Position)
        checkWinner() -> Player
* */

public class TicTacToe {
    private Player player1;
    private Player player2;
    private Player currPlayer;
    private Board board;
    private GameStatus gameStatus;

    public TicTacToe(Player player1, Player player2, Board board) {
        this.player1 = player1;
        this.player2 = player2;
        this.currPlayer = null;
        this.board = board;
        this.gameStatus = GameStatus.NOT_STARTED;
    }

    public Player startGame() {
        this.gameStatus = GameStatus.IN_PROGRESS;

        while(true) {
            if(currPlayer == null || currPlayer == player2) {
                currPlayer = player1;
            } else {
                currPlayer = player2;
            }
            playTurn(currPlayer, new Position(0, 0));

            Player winner = checkWinner();
            if(winner != null) {
                gameStatus = GameStatus.WON;
                return winner;
            }

            if(board.isBoardFull()) {
                gameStatus = GameStatus.DRAW;
                break;
            }
        }

        return null;
    }

    public void playTurn(Player player, Position position) {
        board.markPosition(position, player.getMark());
    }

    public Player checkWinner() {
        Mark winnerMark = board.checkWinner();

        if(winnerMark == null) return null;
        return winnerMark == player1.getMark() ? player1 : player2;
    }
}

/*

Board has-a grid of Mark

TicTacToe has-a Player player1
TicTacToe has-a Player player2
TicTacToe has-a Player currPlayer
TicTacToe has-a Board
TicTacTow has-a GameStatus

* */