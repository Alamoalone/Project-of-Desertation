package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    TeamColor teamTurn;
    ChessBoard board;

    public ChessGame() {
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition){
        ChessPiece piece = this.board.getPiece(startPosition);
        ChessBoard copyBoard = null;
        if(piece == null || piece.getTeamColor() != this.getTeamTurn()){
            return null;
        }
        Collection<ChessMove> moves = piece.pieceMoves(this.board, startPosition);
        Collection<ChessMove> reMoves = new HashSet<>();
        for (ChessMove move : moves) {
            try {
                copyBoard = (ChessBoard) this.board.clone();
            } catch (CloneNotSupportedException ex) {
                System.out.println("Board not cloneable");
            }
            if(copyBoard != null) {
                this.board.addPiece(move.getEndPosition(), this.board.getPiece(move.getStartPosition()));
                this.board.addPiece(move.getStartPosition(), null);
                if (this.isInCheck(this.getTeamTurn())) {
                    reMoves.add(move);
                }
                this.board = copyBoard;
            }
        }

        for(ChessMove reMove : reMoves){
            moves.remove(reMove);
        }

        return moves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        Collection<ChessMove> validPieceMoves = this.validMoves(move.getStartPosition());
        if(validPieceMoves.contains(move)){
            if(move.getPromotionPiece() == null) {
                this.board.addPiece(move.getEndPosition(), this.board.getPiece(move.getStartPosition()));
                this.board.addPiece(move.getStartPosition(), null);
            } else{
                this.board.addPiece(move.getEndPosition(), new ChessPiece(this.teamTurn, move.getPromotionPiece()));
                this.board.addPiece(move.getStartPosition(), null);
            }
            if(this.teamTurn == TeamColor.WHITE){
                this.teamTurn = TeamColor.BLACK;
            } else{
                this.teamTurn = TeamColor.WHITE;
            }
        } else{
            throw new InvalidMoveException();
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPiece king = new ChessPiece(teamColor, ChessPiece.PieceType.KING);
        ChessPiece enemy;
        ChessPosition position = new ChessPosition(1,1);
        ChessPiece currentPiece = this.board.getPiece(position);
        Collection<ChessMove> potentialMoves;
        PieceMovesCalculator mover;
        ChessMove nextMove;
        boolean inCheck = false;

        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                position = new ChessPosition(i, j);
                currentPiece = this.board.getPiece(position);
                if(king.equals(currentPiece)){
                    break;
                } else if(position.getRow() == 8 && position.getColumn() == 8){
                    return false;
                }
            }
            if(king.equals(currentPiece)){
                break;
            }
        }




        mover = new KnightMoveCalculator();
        potentialMoves = mover.pieceMoves(this.board,position);

        for (ChessMove potentialMove : potentialMoves) {
            enemy = this.board.getPiece(potentialMove.getEndPosition());
            if (enemy != null && enemy.getPieceType() == ChessPiece.PieceType.KNIGHT){
                return true;
            }
        }

        mover = new RookMovesCalculator();
        potentialMoves = mover.pieceMoves(this.board,position);

        for (ChessMove potentialMove : potentialMoves) {
            enemy = this.board.getPiece(potentialMove.getEndPosition());
            if ( enemy != null && (enemy.getPieceType() == ChessPiece.PieceType.ROOK || enemy.getPieceType() == ChessPiece.PieceType.QUEEN)){
                return true;
            }
        }

        mover = new BishopMovesCalculator();
        potentialMoves = mover.pieceMoves(this.board,position);

        for (ChessMove potentialMove : potentialMoves) {
            enemy = this.board.getPiece(potentialMove.getEndPosition());
            if ( enemy != null && (enemy.getPieceType() == ChessPiece.PieceType.BISHOP || enemy.getPieceType() == ChessPiece.PieceType.QUEEN)){
                return true;
            }
        }

        if((this.teamTurn == TeamColor.WHITE && position.getRow() < 8) ||
                (this.teamTurn == TeamColor.BLACK && position.getRow() > 1)) {
            mover = new PawnMovesCalculator();
            potentialMoves = mover.pieceMoves(this.board, position);

            for (ChessMove potentialMove : potentialMoves) {
                enemy = this.board.getPiece(potentialMove.getEndPosition());
                if (enemy != null && (enemy.getPieceType() == ChessPiece.PieceType.PAWN)) {
                    return true;
                }
            }
        }


        mover = new KingMovesCalculator();
        potentialMoves = mover.pieceMoves(this.board, position);
        for (ChessMove potentialMove : potentialMoves) {
            enemy = this.board.getPiece(potentialMove.getEndPosition());
            if (enemy != null && (enemy.getPieceType() == ChessPiece.PieceType.KING)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        Collection<ChessMove> tester;
        if(isInCheck(teamColor)){
            for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 8; j++) {
                    ChessPosition position = new ChessPosition(i, j);
                    tester = this.validMoves(position);
                    if((!(tester == null)) && !tester.isEmpty()){
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if(!isInCheck(teamColor)){
            for (int i = 1; i < 8; i++) {
                for (int j = 1; j < 8; j++) {
                    ChessPosition position = new ChessPosition(i, j);
                    if(!(this.validMoves(position) == null)){
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }
}
