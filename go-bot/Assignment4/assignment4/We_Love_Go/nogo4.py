#!/usr/local/bin/python3
# /usr/bin/python3
# Set the path to your python3 above

from gtp_connection import GtpConnection
from board_util import GO_COLOR, GO_POINT, PASS, NULLPOINT, GoBoardUtil
from board import GoBoard
from simulation_engine import GoSimulationEngine, Go3Args
from simulation_util import writeMoves, select_best_move
from ucb import runUcb
from pattern_util import PatternUtil
from board_score import winner

class NoGo(GoSimulationEngine):
    def __init__(self):
        """
        NoGo player that selects moves by simulation.

        Parameters
        ----------
        name : str
            name of the player (used by the GTP interface).
        version : float
            version number (used by the GTP interface).
        """
        super().__init__("NoGo4", 1.0, 100, "", "", False, 5)
        self.moveWins = []
        self.curMoves = []
        self.boardRef = None

    def simulate(self, board: GoBoard, move: GO_POINT, toplay: GO_COLOR) -> GO_COLOR:
        """
        Run a simulated game for a given move.
        """
        cboard: GoBoard = board.copy()
        cboard.play_move(move, toplay)
        opp: GO_COLOR = GoBoardUtil.opponent(toplay)
        return self.playGame(cboard, opp)

    def get_best_move(self):
        if not self.moveWins:
            return GoBoardUtil.generate_random_move(self.boardRef, self.color, True)
        return select_best_move(self.boardRef, self.curMoves, self.moveWins)

    def get_move(self, board: GoBoard, color: GO_COLOR) -> GO_POINT:
        """
        Run one-ply MC simulations to get a move to play.
        """
        self.boardRef = board
        self.color = color
        cboard = board.copy()

        emptyPoints = board.get_empty_points()
        self.curMoves = []
        for p in emptyPoints:
            if board.is_legal(p, color):
                self.curMoves.append(p)
        if not self.curMoves:
            return PASS
        self.curMoves.append(PASS)
        if self.args.use_ucb:
            C = 0.4  # sqrt(2) is safe, this is more aggressive
            best = runUcb(self, cboard, C, self.curMoves, color)
            return best
        else:
            self.moveWins = []
            for move in self.curMoves:
                wins = self.simulateMove(cboard, move, color)
                self.moveWins.append(wins)
            writeMoves(cboard, self.curMoves, self.moveWins, self.args.sim)
            return select_best_move(board, self.curMoves, self.moveWins)

    def playGame(self, board: GoBoard, color: GO_COLOR) -> GO_COLOR:
        """
        Run a simulation game.
        """
        nuPasses = 0
        for _ in range(self.args.limit):
            color = board.current_player
            if self.args.random_simulation:
                move = GoBoardUtil.generate_random_move(board, color, True)
            else:
                move = PatternUtil.generate_move_with_filter(
                    board, self.args.use_pattern, self.args.check_selfatari
                )
            board.play_move(move, color)
            if move == PASS:
                move = GoBoardUtil.generate_random_move(board, color, True)
                if move == PASS:
                    nuPasses += 1
            else:
                nuPasses = 0
            if nuPasses >= 2:
                break
        return winner(board, self.komi)

def run():
    """
    start the gtp connection and wait for commands.
    """
    board = GoBoard(7)
    con = GtpConnection(NoGo(), board)
    con.start_connection()

if __name__ == "__main__":
    run()
