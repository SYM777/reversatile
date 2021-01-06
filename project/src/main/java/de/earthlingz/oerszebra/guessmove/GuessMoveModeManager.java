package de.earthlingz.oerszebra.guessmove;

import androidx.annotation.Nullable;
import com.shurik.droidzebra.*;
import de.earthlingz.oerszebra.BoardView.AbstractBoardViewModel;
import de.earthlingz.oerszebra.GameSettingsConstants;
import de.earthlingz.oerszebra.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;


public class GuessMoveModeManager extends AbstractBoardViewModel {

    private final ZebraEngine engine;
    private EngineConfig generatorConfig;
    private EngineConfig guesserConfig;
    private GameState gameState = new GameState(8);
    private Random random = new Random();
    private CandidateMove[] candidateMoves = new CandidateMove[0];
    private BoardViewModelListener listener = new BoardViewModelListener() {
    };
    private GuessMoveListener guessMoveListener;

    private Context context;

    Cursor userCursor;
    long userId=0;


    public GuessMoveModeManager(ZebraEngine engine, EngineConfig globalSettings, Context context) {
        this.engine = engine;
        initConfigs(globalSettings);
        this.context = context;
    }

    private void initConfigs(EngineConfig globalSettings) {
        generatorConfig = createGeneratorConfig(globalSettings);
        guesserConfig = createGuesserConfig(globalSettings);
    }

    private static EngineConfig createGuesserConfig(EngineConfig gs) {
        return new EngineConfig(
                GameSettingsConstants.FUNCTION_HUMAN_VS_HUMAN,
                8, 16, 1, false, "",   // 20   22
                false,
                true,
                false,
                1,
                1,
                0,
                0
        );
    }

    private static EngineConfig createGeneratorConfig(EngineConfig gs) {

        return new EngineConfig(
                GameSettingsConstants.FUNCTION_ZEBRA_VS_ZEBRA,
                8, 12, 1, true,
                gs.forcedOpening,
                false,
                false,
                gs.useBook,
                2,  //1
                1,  //1
                0,
                0
        );
    }

// 15.10.2019 SYM777:
    public static class GlobalVars
    {
        public static String CandidateMovesForBoardView = "";   // список попыток (ходов) отгадать лучший ход в режиме GuessMove
        public static boolean GuessMode = false;                // Флаг режима GuessMove
        public static boolean GuessMoveActivityIsOn = false;    // Флаг активити GuessMove
        public static int BackgroundColor = 0;                  // Индекс цвета фона

        static int movesCount0 = 0;         // число ходов перед записью в БД
        static String moveSequence0;        // последовательность ходов перед записью в БД
        static int movesCount1 = 0;         // число ходов после чтения из БД (для воспроизведения)
        public static String moveSequence1;        // последовательность ходов после чтения из БД (для воспроизведения)
        public static boolean Restore = false;     // флаг необходимости воспроизведения (после выбора из списка)
        static boolean GameFromBad = false; // флаг при повторной загрузке задания из БД (bad > 2 или hint > 2)

        public static GameState gameStateGuess = new GameState(8);  // для обращения из других классов

        static SQLiteDatabase db;
    }



    void generate(int minIn, int max, GuessMoveListener guessMoveListener, boolean Reload) {
        int min = Math.max(minIn, 4);
        this.guessMoveListener = guessMoveListener;
        final int movesPlayed = random.nextInt(max + 1 - min) + min + 2;

//        Log.d("SYM777_DEBUG =====>", "min = " + min + ", max = " + max + ", movesPlayed = " + movesPlayed);

        this.candidateMoves = new CandidateMove[0];
        new GameGenerator(engine, context).generate(generatorConfig, guesserConfig, movesPlayed, gameState -> {
            GuessMoveModeManager.this.gameState = gameState;
            GlobalVars.gameStateGuess = gameState;
            gameState.setGameStateListener(new GameStateListener() {
                @Override
                public void onBoard(GameState board) {
                    listener.onBoardStateChanged();
                    updateCandidateMoves(board.getCandidateMoves());
                    guessMoveListener.onSideToMoveChanged(board.getSideToMove());
                }
            });
            guessMoveListener.onGenerated(gameState.getSideToMove());
            listener.onBoardStateChanged();

// 15.10.2019 SYM777: to prevent premature display of eval-values
// CandidateMovesForBoardView - список попыток (ходов) отгадать лучший ход в режиме GuessMove
// The error is that arrays CandidateMoves in GuessMoveModeManager and in BoardView are different
            GlobalVars.CandidateMovesForBoardView = "";
        }, Reload);

    }

    private void updateCandidateMoves(CandidateMove[] newCandidates) {
        ArrayList<CandidateMove> replacement = new ArrayList<>(this.candidateMoves.length);
        for (CandidateMove newCandidate : newCandidates) {
            for (CandidateMove oldCandidates : this.candidateMoves) {
                if (oldCandidates.getMoveInt() == newCandidate.getMoveInt()) {
                    replacement.add(newCandidate);
                }
            }
        }
        if (this.candidateMoves.length == replacement.size()) {
            this.candidateMoves = replacement.toArray(this.candidateMoves);
        } else {
            this.candidateMoves = replacement.toArray(new CandidateMove[replacement.size()]);
        }
    }

    void guess(Move move) {
        String eval = "";

        Utils mUtils = new Utils();

//        Log.d(TAG, "move = " + move.getText());

        if (move == null) {
            guessMoveListener.onBadGuess();
            return;
        }

        GlobalVars.moveSequence0 = gameState.getMoveSequenceAsString();
        GlobalVars.movesCount0 = gameState.getDisksPlayed();

//        Toast.makeText(context, "d = " + gameState.getDisksPlayed() + " ... m0 = " + GlobalVars.movesCount0 + " ... m1 = " + GlobalVars.movesCount1, Toast.LENGTH_SHORT).show();

// 15.10.2019 SYM777: ignore repeated clicking on the previously selected field
/*        for (CandidateMove candidateMove : this.candidateMoves) {
            if (!GlobalVars.CandidateMovesForBoardView.contains(move.getText())) {
                GlobalVars.CandidateMovesForBoardView = GlobalVars.CandidateMovesForBoardView + " " + move.getText();
            }

            if (candidateMove.getMoveInt() == move.getMoveInt()) {
                if (candidateMove.isBest) {
                    GlobalVars.GuessMode = 0;
                    showAllMoves();
                    gameState.setGameStateListener(new GameStateListener() {
                        @Override
                        public void onBoard(GameState board) {
                            candidateMoves = board.getCandidateMoves();
                            listener.onBoardStateChanged();
                            guessMoveListener.onSideToMoveChanged(board.getSideToMove());
                        }
                    });

                    this.guessMoveListener.onCorrectGuess();
                    mUtils.saveBD(context, 0, GlobalVars.movesCount0, GlobalVars.moveSequence0, 1, 0);
                    return;
                }
                else return;
            }
        }*/

// 15.10.2019 SYM777: processing the first pressing
        for (CandidateMove candidateMove : gameState.getCandidateMoves()) {
            if (!GlobalVars.CandidateMovesForBoardView.contains(move.getText())) {
                GlobalVars.CandidateMovesForBoardView = GlobalVars.CandidateMovesForBoardView + " " + move.getText();
            }

            if (move.getMoveInt() == candidateMove.getMoveInt()) {
                if (candidateMove.isBest) {
                    GlobalVars.GuessMode = false;
                    showAllMoves();
                    gameState.setGameStateListener(new GameStateListener() {
                        @Override
                        public void onBoard(GameState board) {
                            candidateMoves = board.getCandidateMoves();
                            listener.onBoardStateChanged();
                            guessMoveListener.onSideToMoveChanged(board.getSideToMove());
                        }
                    });

                    guessMoveListener.onCorrectGuess();
                    mUtils.saveBDguess(context, 0, GlobalVars.movesCount0, GlobalVars.moveSequence0, true, false, false, 0);
                    guessMoveListener.onShowStats(GuessMoveModeManager.GlobalVars.moveSequence0);

                    return;
                } else {
                    if (candidateMove.hasEval) eval = candidateMove.evalShort;
                }
            }
        }
        showMove(move);
        guessMoveListener.onBadGuess();
        if (eval.length() > 0) {
            mUtils.saveBDguess(context, 0, GlobalVars.movesCount0, GlobalVars.moveSequence0, false, true, false, Integer.valueOf(eval));
            guessMoveListener.onShowStats(GuessMoveModeManager.GlobalVars.moveSequence0);

        }
        else
            Toast.makeText(context, "На " + move.getText() + " хода нет...", Toast.LENGTH_SHORT).show();
    }

    public void move(Move move) throws InvalidMove {
        this.engine.makeMove(gameState, move);
    }


    void hintGuess() {
        GlobalVars.moveSequence0 = gameState.getMoveSequenceAsString();
        GlobalVars.movesCount0 = gameState.getDisksPlayed();

        GlobalVars.GuessMode = false;
        showAllMoves();
        gameState.setGameStateListener(new GameStateListener() {
            @Override
            public void onBoard(GameState board) {
                candidateMoves = board.getCandidateMoves();
                listener.onBoardStateChanged();
                guessMoveListener.onSideToMoveChanged(board.getSideToMove());
            }
        });

        guessMoveListener.onHintGuess();
        Utils mUtils = new Utils();
        mUtils.saveBDguess(context, 0, GuessMoveModeManager.GlobalVars.movesCount0, GuessMoveModeManager.GlobalVars.moveSequence0, false, false, true, 0);
        guessMoveListener.onShowStats(GuessMoveModeManager.GlobalVars.moveSequence0);
    }



    void redoMove() {
        engine.redoMove(gameState);
    }

    void undoMove() {
        engine.undoMove(gameState);
    }

    @Override
    public int getBoardSize() {
        return 8;
    }

    @Nullable
    @Override
    public Move getLastMove() {
        return new Move(gameState.getLastMove());
    }

    @Override
    public CandidateMove[] getCandidateMoves() {
        return this.candidateMoves;
    }

    @Override
    public boolean isValidMove(Move move) {
        for (CandidateMove candidateMove : gameState.getCandidateMoves()) {
            if (candidateMove.getMoveInt() == move.getMoveInt()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Move getNextMove() {
        return new Move(gameState.getNextMove());
    }

    @Override
    public void setBoardViewModelListener(BoardViewModelListener boardViewModelListener) {
        this.listener = boardViewModelListener;
    }

    @Override
    public void removeBoardViewModeListener() {
        this.listener = new BoardViewModelListener() {
        };
    }

    @Override
    public boolean isFieldFlipped(int x, int y) {
        return false;
    }

    @Override
    public Player playerAt(int x, int y)
    {
        int player = gameState.getByteBoard().get(x,y);
        return Player.values()[player];
    }

    public void updateGlobalConfig(EngineConfig engineConfig) {
        initConfigs(engineConfig);
        //TODO update config for current game

    }

    private void showMove(Move move) {
        for (CandidateMove candidateMove : this.candidateMoves) {
            if (candidateMove.getMoveInt() == move.getMoveInt()) {

//                Log.d(TAG, "1 candidateMove.getMoveInt() = " + candidateMove.getMoveInt() + "move.getMoveInt() = " + move.getMoveInt());

                return;
            }
        }
        for (CandidateMove candidateMove : this.gameState.getCandidateMoves()) {
            if (candidateMove.getMoveInt() == move.getMoveInt()) {

//                Log.d(TAG, "2 candidateMove.getMoveInt() = " + candidateMove.getMoveInt() + "move.getMoveInt() = " + move.getMoveInt());

                CandidateMove[] newCandidateMoves = Arrays.copyOf(candidateMoves, candidateMoves.length + 1);
                newCandidateMoves[newCandidateMoves.length - 1] = candidateMove;
                this.candidateMoves = newCandidateMoves;
                this.listener.onCandidateMovesChanged();
                break;
            }
        }

    }

    private void showAllMoves() {
        this.candidateMoves = gameState.getCandidateMoves();
        listener.onCandidateMovesChanged();
    }

    public interface GuessMoveListener {
        void onGenerated(int sideToMove);

        void onSideToMoveChanged(int sideToMove);

        void onCorrectGuess();

        void onBadGuess();

        void onHintGuess();

        void onShowStats(String moveseq);

    }
}
