import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class AI {

    /** Region score given to each region.
     *  Region score should equal be 0. */
    private final int region1 = 6;
    private final int region2 = -2;
    private final int region3 = 15;
    private final int region4 = -5;
    private final int region5 = 60;

    private final int[][] dirS = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}};
    private final int[][] dir = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {-1, 1}, {1, -1}};
    /** The number of moves that this AI will search into. */
    private final int depth = 9;
    /** Postiive infinity. */
    private final int INFTY = Integer.MAX_VALUE;
    /** Negative infinity. */
    private final int NINFTY = Integer.MIN_VALUE;

    /** Initializes an AI that plays the player PIECE in board B. */
    public AI(Board b, int piece) {
        _board = b;
        _piece = piece;
        _heuristic = new int[b.size()][b.size()];
        runHeuristic();
    }

    /** Returns the next move for the AI. */
    public int[] getNextMove() {
        int nextMove = alphaBeta(NINFTY, INFTY, depth, true);
        return new int[]{nextMove / _board.size(), nextMove % _board.size()};
    }

    /** Recursively searches the possible moves that the AI can take with depth D.
     *  Is maximizer if MAXIMIZER is TRUE. */
    private int alphaBeta(int alpha, int beta, int d, boolean maximizer) {
        if (_board.gameOver()) {
            if (_board.getCount(_piece) < _board.getCount(_board.oppositePiece(_piece))) {
                return NINFTY + 100;
            } else if (_board.getCount(_piece) > _board.getCount(_board.oppositePiece(_piece))) {
                return INFTY - 100;
            } else {
                return 0;
            }
        }
        if (d == 0) {
            return evaluateBoard();
        }
        int best;
        int[] nextMove = new int[]{0, -1};
        if (maximizer) {
            best = NINFTY;
            PriorityQueue<int[]> nextMoves = nextMoves(_piece);
            if (nextMoves.size() == 0) {
                return alphaBeta(alpha, beta, d, false);
            } else {
                for (int[] next :nextMoves) {
                    _board.makeMove(_piece, next[0], next[1]);
                    int n = alphaBeta(alpha, beta, d - 1, false);
                    _board.undo();
                    if (d == depth && best < n) {
                        nextMove = next;
                    }
                    best = Math.max(best, n);
                    alpha = Math.max(alpha, best);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
        } else {
            best = INFTY;
            PriorityQueue<int[]> nextMoves = nextMoves(_board.oppositePiece(_piece));
            if (nextMoves.size() == 0) {
                return alphaBeta(alpha, beta, d, true);
            } else {
                for (int[] next : nextMoves) {
                    _board.makeMove(_board.oppositePiece(_piece), next[0], next[1]);
                    int n = alphaBeta(alpha, beta, d - 1, true);
                    _board.undo();
                    if (d == depth && best > n) {
                        nextMove = next;
                    }
                    best = Math.min(best, n);
                    beta = Math.min(beta, best);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
        }
        if (d == depth) {
            return nextMove[0] * _board.size() + nextMove[1];
        }
        return best;
    }

    /** Returns a PriorityQueue of possible moves that AI can take based on the heuristic of the board that the move will make.*/
    public PriorityQueue<int[]> nextMoves(int piece) {
        PriorityQueue<int[]> nextMoves = new PriorityQueue<>(_board.size() * _board.size(), new Comp());
        for (int i = 0; i < _board.size(); i++) {
            for (int j = 0; j < _board.size(); j++) {
                if (_board.isLegalMove(piece, i, j)) {
                    nextMoves.add(new int[]{i, j});
                }
            }
        }
        return nextMoves;
    }

    /** Comparator that returns 1 if heuristic score of X is smaller than the heuristic score of Y.
     *  Returns -1 otherwise. */
    private class Comp implements Comparator<int[]> {
        public int compare(int[] x, int[] y) {
            if (_heuristic[x[0]][x[1]] < _heuristic[y[0]][y[1]]) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    /** Evaluates the score of the board based on the heuristic. */
    public int evaluateBoard() {
        int score = 0;
        for (int i = 0; i < _board.size(); i++) {
            for (int j = 0; j < _board.size(); j++) {
                if (_board.get(i, j) == _piece) {
                    score += _heuristic[i][j];
                } else if (_board.get(i, j) == _board.oppositePiece(_piece)) {
                    score -= _heuristic[i][j];
                }
            }
        }
        return score;
    }

    private void runHeuristic() {
        for (int i = 0; i < _board.size(); i++) {
            for (int j = 0; j < _board.size(); j++) {
                if (_board.isOutofBound(i, j)) {
                    _heuristic[i][j] = -1000;
                } else if (isCorner5(i, j)) {
                    _heuristic[i][j] = region5;
                    for (int[] d : dir) {
                        if (0 <= i + d[0] && i + d[0] < _heuristic.length && 0 <= j + d[1] && j + d[1] < _heuristic.length
                                && _heuristic[i + d[0]][j + d[1]] != region5 && !_board.isOutofBound(i + d[0], j + d[1])
                                && !_board.isOutofBound(i + 2 * d[0], j + 2 * d[1])) {
                            _heuristic[i + d[0]][j + d[1]] = region4;
                        }
                    }
                }
            }
        }
        for (int i = 0; i < _board.size(); i++) {
            for (int j = 0; j < _board.size(); j++) {
                if (_heuristic[i][j] == 0 && isCorner3(i, j)) {
                    _heuristic[i][j] = region3;
                    for (int[] d : dir) {
                        if (0 <= i + d[0] && i + d[0] < _heuristic.length && 0 <= j + d[1] && j + d[1] < _heuristic.length
                                && _heuristic[i + d[0]][j + d[1]] == 0 && !_board.isOutofBound(i + d[0], j + d[1])
                                && !_board.isOutofBound(i + 2 * d[0], j + 2 * d[1])) {
                            _heuristic[i + d[0]][j + d[1]] = region2;
                        }
                    }
                }
            }
        }
        ArrayList<int[]> toAdd = new ArrayList<>();
        for (int i = 0; i < _board.size(); i++) {
            for (int j = 0; j < _board.size(); j++) {
                if (_heuristic[i][j] == 0) {
                    _heuristic[i][j] = region1;
                } else if (_heuristic[i][j] == region3) {
                    for (int n = 0; n < 4; n++) {
                        if (0 <= i + 3 * dir[n][0] && i + 3 * dir[n][0] < _heuristic.length && 0 <= j + 3 * dir[n][1] && j + 3 * dir[n][1] < _heuristic.length
                                && _heuristic[i + 3 * dir[n][0]][j + 3 * dir[n][1]] == region3
                                && _heuristic[i + dir[n][0]][j + dir[n][1]] == region2
                                && _heuristic[i + 2 * dir[n][0]][j + 2 * dir[n][1]] == region2) {
                            toAdd.add(new int[]{i + dir[n][0], j + dir[n][1]});
                            toAdd.add(new int[]{i + 2 * dir[n][0], j + 2 * dir[n][1]});
                        }
                    }
                }
            }
        }
        for (int[] a : toAdd) {
            _heuristic[a[0]][a[1]] = region3;
        }
    }

    /** Prints the board with marked regions. */
    public void printHeuristic() {
        for (int i = 0; i < _heuristic.length; i++) {
            StringBuilder line = new StringBuilder();
            for (int j = 0; j < _heuristic.length; j++) {
                if (_heuristic[j][i] == -1000) {
                    line.append("X ");
                } else if (_heuristic[j][i] == region1) {
                    line.append("1 ");
                } else if (_heuristic[j][i] == region2) {
                    line.append("2 ");
                } else if (_heuristic[j][i] == region3) {
                    line.append("3 ");
                } else if (_heuristic[j][i] == region4) {
                    line.append("4 ");
                } else if (_heuristic[j][i] == region5) {
                    line.append("5 ");
                } else {
                    line.append(_heuristic[j][i]);
                    line.append(" ");
                }
            }
            System.out.println(line);
        }
    }

    /** Returns TRUE if (X, Y) is the corner that cannot be flipped once occupied. */
    private boolean isCorner5(int x, int y) {
        for (int[] i : dirS) {
            if (_board.get(x + i[0], y + i[1]) != -1 && _board.get(x - i[0], y - i[1]) != -1) {
                return false;
            }
        }
        return true;
    }

    /** Returns TRUE if (X, Y) is the corner that cannot be flipped once occupied assuming that region 4 and 5 are filled. */
    private boolean isCorner3(int x, int y) {
        for (int[] i : dirS) {
            if (!(_board.get(x + i[0], y + i[1]) == -1 || _heuristic[x + i[0]][y + i[1]] == region4 || _heuristic[x + i[0]][y + i[1]] == region5)
                    && !(_board.get(x - i[0], y - i[1]) == -1 || _heuristic[x - i[0]][y - i[1]] == region4 || _heuristic[x - i[0]][y - i[1]] == region5)) {
                return false;
            }
        }
        return true;
    }

    /** The number that this AI plays. */
    private int _piece;
    /** The board. */
    private Board _board;
    /** An array that stores the heuristic of each location on the board. */
    private int[][] _heuristic;
}
