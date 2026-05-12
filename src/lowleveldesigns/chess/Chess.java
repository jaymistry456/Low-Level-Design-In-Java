package lowleveldesigns.chess;

/*
enums
* */
/*
GameStatus (ACTIVE, OVER, STALEMATE)
PieceColor (WHITE, BLACK)
* */

enum GameStatus {
    ACTIVE,
    OVER,
    STALEMATE;
}

enum PieceColor {
    BLACK,
    WHITE;
}

/*
classes
* */
/*

Player
Position (record)
ChessPiece
King
Queen
Rook
Bishop
Knight
Pawn
Board
Chess

* */


/*
Player
    knows:
        playerId
        playerName
        PieceColor
    does:
        nothing (date carrier)
* */
class Player {
    private String playerId;
    private String playerName;
    private PieceColor color;

    public Player(String playerId, String playerName, PieceColor color) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.color = color;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public PieceColor getPieceColor() {
        return color;
    }
}

/*
Position (record class with row and col)
 * */
record Position(int row, int col) {}

/*
ChessPiece
    knows:
        Position
        PieceColor
    does:
        isValidMove(Position, Board) -> boolean
 * */
abstract class ChessPiece {
    private Position position;
    private PieceColor color;

    public ChessPiece(Position position, PieceColor color) {
        this.position = position;
        this.color = color;
    }

    public abstract boolean isValidMove(Position position, Board board);

    public Position getPosition() {
        return position;
    }

    public PieceColor getColor() {
        return color;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}

/*
King
    knows:
    does:
        isValidMove(Position, Board) -> boolean
 * */
class King extends ChessPiece {
    public King(Position position, PieceColor color) {
        super(position, color);
    }

    @Override
    public boolean isValidMove(Position newPosition, Board board) {
        if(!board.isWithinBounds(newPosition)) return false;

        if(Math.abs(newPosition.row() - getPosition().row()) > 1
                || Math.abs(newPosition.col() - getPosition().col()) > 1) {
            return false;
        }

        ChessPiece target = board.getPiece(newPosition);
        if(target != null && target.getColor() == this.getColor()) return false;

        return true;
    }
}

/*
Queen
    knows:
    does:
        isValidMove(Position, Board) -> boolean
 * */
class Queen extends ChessPiece {
    public Queen(Position position, PieceColor color) {
        super(position, color);
    }

    @Override
    public boolean isValidMove(Position newPosition, Board board) {
        if(!board.isWithinBounds(newPosition)) return false;

        Rook rookBehavior = new Rook(getPosition(), getColor());
        Bishop bishopBehavior = new Bishop(getPosition(), getColor());

        return rookBehavior.isValidMove(newPosition, board) ||
                bishopBehavior.isValidMove(newPosition, board);
    }
}

/*
Rook
    knows:
    does:
        isValidMove(Position, Board) -> boolean
 * */
class Rook extends ChessPiece {
    public Rook(Position position, PieceColor color) {
        super(position, color);
    }

    @Override
    public boolean isValidMove(Position newPosition, Board board) {
        if(!board.isWithinBounds(newPosition)) return false;

        Position currPosition = getPosition();

        int rowStart = Math.min(currPosition.row(), newPosition.row());
        int rowEnd = Math.max(currPosition.row(), newPosition.row());
        int colStart = Math.min(currPosition.col(), newPosition.col());
        int colEnd = Math.max(currPosition.col(), newPosition.col());

        if(rowStart != rowEnd && colStart != colEnd) return false;

        if(rowStart == rowEnd) {
            int start = colStart;
            int end = colEnd;

            for(int i = start + 1; i < end; i++) {
                ChessPiece currPiece = board.getPiece(new Position(rowStart, i));
                if(currPiece != null) return false;
            }

            ChessPiece target = board.getPiece(new Position(rowEnd, colEnd));
            if(target != null && target.getColor() == getColor()) return false;
        }
        else {
            int start = rowStart;
            int end = rowEnd;

            for(int i = start + 1; i < end; i++) {
                ChessPiece currPiece = board.getPiece(new Position(i, colStart));
                if(currPiece != null) return false;
            }

            ChessPiece target = board.getPiece(new Position(rowEnd, colEnd));
            if(target != null && target.getColor() == getColor()) return false;
        }

        return true;
    }
}

/*
Bishop
    knows:
    does:
        isValidMove(Position, Board) -> boolean
 * */
class Bishop extends ChessPiece {
    public Bishop(Position position, PieceColor color) {
        super(position, color);
    }

    @Override
    public boolean isValidMove(Position newPosition, Board board) {
        if(!board.isWithinBounds(newPosition)) return false;

        int rowDiff = newPosition.row() - getPosition().row();
        int colDiff = newPosition.col() - getPosition().col();

        if(Math.abs(rowDiff) != Math.abs(colDiff)) return false;

        int rowDir = rowDiff > 0 ? 1 : -1;
        int colDir = colDiff > 0 ? 1 : -1;

        int r = getPosition().row() + rowDir;
        int c = getPosition().col() + colDir;

        while(r != newPosition.row() && c != newPosition.col()) {
            if(board.getPiece(new Position(r, c)) != null) return false;
            r += rowDir;
            c += colDir;
        }

        ChessPiece target = board.getPiece(newPosition);
        if(target != null && target.getColor() == getColor()) return false;

        return true;
    }
}

/*
Knight
    knows:
    does:
        isValidMove(Position, Board) -> boolean
 * */
class Knight extends ChessPiece {
    public Knight(Position position, PieceColor color) {
        super(position, color);
    }

    @Override
    public boolean isValidMove(Position newPosition, Board board) {
        if(!board.isWithinBounds(newPosition)) return false;

        int rowDiff = Math.abs(newPosition.row() - getPosition().row());
        int colDiff = Math.abs(newPosition.col() - getPosition().col());

        if(!((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2))) return false;

        ChessPiece target = board.getPiece(newPosition);
        if(target != null && target.getColor() == getColor()) return false;

        return true;
    }
}

/*
Pawn
    knows:
    does:
        isValidMove(Position, Board) -> boolean
 * */
class Pawn extends ChessPiece {
    public Pawn(Position position, PieceColor color) {
        super(position, color);
    }

    @Override
    public boolean isValidMove(Position newPosition, Board board) {
        if(!board.isWithinBounds(newPosition)) return false;

        int direction = getColor() == PieceColor.WHITE ? 1 : -1;
        int rowDiff = newPosition.row() - getPosition().row();
        int colDiff = Math.abs(newPosition.col() - getPosition().col());

        // move forward one square
        if(colDiff == 0 && rowDiff == direction) {
            return board.getPiece(newPosition) == null;
        }

        // move forward two squares from starting row
        int startRow = getColor() == PieceColor.WHITE ? 1 : 6;
        if(colDiff == 0 && rowDiff == 2 * direction && getPosition().row() == startRow) {
            Position middle = new Position(getPosition().row() + direction, getPosition().col());
            return board.getPiece(middle) == null && board.getPiece(newPosition) == null;
        }

        // diagonal capture
        if(colDiff == 1 && rowDiff == direction) {
            ChessPiece target = board.getPiece(newPosition);
            return target != null && target.getColor() != getColor();
        }

        return false;
    }
}

/*
Board
    knows:
        ChessPiece[][] grid (8x8, null means empty square)
    does:
        getPiece(Position) -> ChessPiece
        setPiece(Position, ChessPiece)
        movePiece(Position from, Position to)
        isWithinBounds(Position) -> boolean
 * */
class Board {
    private ChessPiece[][] chessPieces;

    public Board(ChessPiece[][] chessPieces) {
        this.chessPieces = chessPieces;
    }

    public ChessPiece[][] getChessPieces() {
        return chessPieces;
    }

    public ChessPiece getPiece(Position position) {
        if(!isWithinBounds(position)) return null;
        return chessPieces[position.row()][position.col()];
    }

    public void setPiece(Position position, ChessPiece piece) {
        if(!isWithinBounds(position)) return;
        chessPieces[position.row()][position.col()] = piece;
    }

    public void movePiece(Position from, Position to) {
        if(!isWithinBounds(from) || !isWithinBounds(to)) return;

        ChessPiece piece = getPiece(from);
        setPiece(from, null);
        setPiece(to, piece);
        if(piece != null) {
            piece.setPosition(to);
        }
    }

    public boolean isWithinBounds(Position position) {
        return position.row() >= 0
                && position.row() < chessPieces.length
                && position.col() >= 0
                && position.col() < chessPieces[0].length;
    }

}

/*
Chess
    knows:
        Player A
        Player B
        Board
        GameStatus
        Player currPlayer
    does:
        isInCheck(Player) -> boolean
        isInCheckMate(Player) -> boolean
        isInStalemate(Player) -> boolean
        move(Player, ChessPiece, Position)
        forfeit(Player)
 * */
public class Chess {
    private Player playerA;
    private Player playerB;
    private Board board;
    private GameStatus gameStatus;
    private Player currPlayer;

    public Chess(Player playerA, Player playerB, Board board, Player currPlayer) {
        this.playerA = playerA;
        this.playerB = playerB;
        this.board = board;
        this.gameStatus = GameStatus.ACTIVE;
        this.currPlayer = currPlayer;
    }

    public Player getPlayerA() {
        return playerA;
    }

    public Player getPlayerB() {
        return playerB;
    }

    public Board getBoard() {
        return board;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public Player getCurrPlayer() {
        return currPlayer;
    }

    public boolean isInCheck(Player player) {
        // find the king
        Position kingPosition = null;
        for(int r = 0; r < 8; r++) {
            for(int c = 0; c < 8; c++) {
                ChessPiece piece = board.getPiece(new Position(r, c));
                if(piece instanceof King && piece.getColor() == player.getPieceColor()) {
                    kingPosition = new Position(r, c);
                    break;
                }
            }
        }

        if(kingPosition == null) return false;

        // check if any opponent piece can attack the king
        for(int r = 0; r < 8; r++) {
            for(int c = 0; c < 8; c++) {
                ChessPiece piece = board.getPiece(new Position(r, c));
                if(piece != null && piece.getColor() != player.getPieceColor()) {
                    if(piece.isValidMove(kingPosition, board)) return true;
                }
            }
        }

        return false;
    }

    public boolean isInCheckMate(Player player) {
        if(!isInCheck(player)) return false;
        return hasNoLegalMoves(player);
    }

    public boolean isInStaleMate(Player player) {
        if(isInCheck(player)) return false;
        return hasNoLegalMoves(player);
    }

    private boolean hasNoLegalMoves(Player player) {
        for(int r = 0; r < 8; r++) {
            for(int c = 0; c < 8; c++) {
                ChessPiece piece = board.getPiece(new Position(r, c));
                if(piece != null && piece.getColor() == player.getPieceColor()) {
                    // try every possible destination
                    for(int dr = 0; dr < 8; dr++) {
                        for(int dc = 0; dc < 8; dc++) {
                            Position dest = new Position(dr, dc);
                            if(piece.isValidMove(dest, board)) {
                                // simulate the move
                                ChessPiece captured = board.getPiece(dest);
                                board.movePiece(piece.getPosition(), dest);
                                boolean stillInCheck = isInCheck(player);
                                // undo the move
                                board.movePiece(dest, piece.getPosition());
                                board.setPiece(dest, captured);
                                if(!stillInCheck) return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public void move(Player player, ChessPiece piece, Position newPosition) {
        // validate it's the player's turn
        if(!currPlayer.equals(player)) return;

        // validate game is active
        if(gameStatus != GameStatus.ACTIVE) return;

        // validate piece belongs to player
        if(piece.getColor() != player.getPieceColor()) return;

        // validate move is legal for this piece
        if(!piece.isValidMove(newPosition, board)) return;

        // simulate move and check if it leaves own King in check
        ChessPiece captured = board.getPiece(newPosition);
        Position oldPosition = piece.getPosition();
        board.movePiece(oldPosition, newPosition);

        if(isInCheck(player)) {
            // undo move
            board.movePiece(newPosition, oldPosition);
            board.setPiece(newPosition, captured);
            return;
        }

        // move is valid — check game state
        Player opponent = player.equals(playerA) ? playerB : playerA;

        if(isInCheckMate(opponent)) {
            gameStatus = GameStatus.OVER;
        } else if(isInStaleMate(opponent)) {
            gameStatus = GameStatus.STALEMATE;
        }

        // switch turns
        currPlayer = opponent;
    }

    public void forfeit(Player player) {
        gameStatus = GameStatus.OVER;
    }
}


/*

Player has-a PieceColor

ChessPiece has-a Position
ChessPiece has-a PieceColor

King is-a ChessPiece
Queen is-a ChessPiece
Rook is-a ChessPiece
Bishop is-a ChessPiece
Knight is-a ChessPiece
Pawn is-a ChessPiece

Board has-a Array of ChessPieces

Chess has-a a Player A and has-a Player B
Chess has-a Board
Chess has-a GameStatus

* */
