import java.util.ArrayList;
import java.util.LinkedList;

public class Board {

    final private int BOARD_SIZE;
    final private int[][] dir = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};

    /** Initializes a board with size 8.
     *  Coordinate of the upper left corner is (0, 0).*/
    public Board() {
        BOARD_SIZE = 8;
        _board = new int[BOARD_SIZE][BOARD_SIZE];
        _history = new LinkedList<>();
        _count = new int[3];
    }

    /** Initializes a board with size SIZE.
     *  Coordinate of the upper left corner is (0, 0).*/
    public Board(int size) {
        BOARD_SIZE = size;
        _board = new int[BOARD_SIZE][BOARD_SIZE];
        _history = new LinkedList<>();
        _count = new int[3];
    }

    /** Returns the size of the board. */
    public int size() {
        return BOARD_SIZE;
    }

    /** Returns 2 if PIECE is 1 and 1 if PIECE is 2. */
    public int oppositePiece(int piece) {
        return 2 - piece + 1;
    }

    /** Adds PIECE to (X, Y) location of the board. */
    public void addPiece(int piece, int x, int y) {
        if (_board[x][y] == -1) {
            _count[2] -= 1;
        } else if (_board[x][y] == 1) {
            _count[0] -= 1;
        } else if (_board[x][y] == 2) {
            _count[1] -= 1;
        }
        _board[x][y] = piece;
        if (piece == -1) {
            _count[2] += 1;
        } else if (piece == 1) {
            _count[0] += 1;
        } else if (piece == 2) {
            _count[1] += 1;
        }
    }

    /** Removes the piece at (X, Y) location of the board.
     *  Does not do anything if it is empty. */
    public void removePiece(int x, int y) {
        addPiece(0, x, y);
    }

    /** Returns TRUE if (X, Y) is not a valid location. */
    public boolean isOutofBound(int x, int y) {
        return get(x, y) == -1;
    }

    /** Returns TRUE if (X, Y) location is empty. */
    public boolean isEmpty(int x, int y) {
        return get(x, y) == 0;
    }

    /** Returns the piece at (X, Y) location of the board. */
    public int get(int x, int y) {
        try {
            return _board[x][y];
        } catch (IndexOutOfBoundsException e) {
            return -1;
        }
    }

    /** Returns TRUE if (X, Y) is a valid move for player PIECE. */
    public boolean isLegalMove(int piece, int x, int y) {
        if (get(x, y) == 0) {
            for (int[] i : dir) {
                int steps = 1;
                int next = get(x + i[0], y + i[1]);
                if (next == oppositePiece(piece)) {
                    while (next != -1 && next != 0) {
                        if (next == piece) {
                            return true;
                        } else {
                            steps += 1;
                            next = get(x + i[0] * steps, y + i[1] * steps);
                        }
                    }
                }
            }
        }
        return false;
    }

    /** Make a player PIECE move at (X, Y). Assumes that the move is valid. */
    public void makeMove(int piece, int x, int y) {
        ArrayList<int[]> origins = new ArrayList<>();
        origins.add(new int[]{x, y});
        for (int[] i : dir) {
            int steps = 1;
            int next = get(x + i[0], y + i[1]);
            boolean complete = false;
            if (next == oppositePiece(piece)) {
                steps += 1;
                next = get(x + i[0] * 2, y + i[1] * 2);
                while (!complete && next != -1 && next != 0) {
                    if (next == piece) {
                        complete = true;
                        origins.add(new int[]{steps, i[0], i[1]});
                    } else {
                        steps += 1;
                        next = get(x + i[0] * steps, y + i[1] * steps);
                    }
                }
            }
            while (complete && steps != 1) {
                steps -= 1;
                addPiece(piece, x + i[0] * steps, y + i[1] * steps);
            }
        }
        if (origins.size() > 1) {
            addPiece(piece, x, y);
            _history.addFirst(origins);
        }
    }

    /** Undo the last player move. */
    public void undo() {
        ArrayList<int[]> last = _history.pollFirst();
        int x = last.get(0)[0];
        int y = last.get(0)[1];
        int piece = oppositePiece(get(x, y));
        for (int i = 1; i < last.size(); i++) {
            int[] next = last.get(i);
            int dx = next[1];
            int dy = next[2];
            for (int j = 1; j < next[0]; j++) {
                addPiece(piece, x + dx * j, y + dy * j);
            }
        }
        addPiece(0, x, y);
    }

    /** Returns True if both players cannot make any more move. */
    public boolean gameOver() {
        for (int i = 0; i < size(); i++) {
            for (int j = 0; j < size(); j++) {
                if (isLegalMove(1, i, j) || isLegalMove(2, i, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Returns the number of pieces that player PIECE has. */
    public int getCount(int piece) {
        return _count[piece - 1];
    }

    /** Prints the board. */
    public void print() {
        for (int i = 0; i < size(); i++) {
            StringBuilder line = new StringBuilder();
            for (int j = 0; j < size(); j++) {
                if (get(j, i) == 0) {
                    line.append("* ");
                } else if (get(j, i) == -1) {
                    line.append("X ");
                } else if (get(j, i) == 1) {
                    line.append("1 ");
                } else if (get(j, i) == 2) {
                    line.append("2 ");
                } else if (get(j, i) == 3) {
                    line.append("O ");
                }
            }
            System.out.println(line);
        }
    }

    /** Int array of size 2 that stores the number of pieces each player has. */
    private int[] _count;
    /** The board. */
    private int[][] _board;
    /** History of the player moves. */
    private LinkedList<ArrayList<int[]>> _history;
}
