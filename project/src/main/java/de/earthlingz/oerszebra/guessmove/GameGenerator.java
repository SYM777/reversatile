package de.earthlingz.oerszebra.guessmove;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.shurik.droidzebra.*;

import java.util.LinkedList;

import de.earthlingz.oerszebra.ZebraServices;
import de.earthlingz.oerszebra.parser.GameParser;

import de.earthlingz.oerszebra.guessmove.GuessMoveModeManager.*;

public class GameGenerator {
    private ZebraEngine engine;

    private Context context;

    public GameParser parser = ZebraServices.getGameParser();

    public GameGenerator(@NonNull ZebraEngine engine, Context context) {
        this.engine = engine;
        this.context = context;
    }

    public void generate(EngineConfig generatorConfig, EngineConfig postConfig, int movesCount, OnGenerated listener, boolean Reload) {

        if (Reload && (GlobalVars.movesCount1 != 0)) {
//            Toast.makeText(context, "конфигурация восстановлена " + GlobalVars.moveSequence1 + " = " + GlobalVars.movesCount1, Toast.LENGTH_SHORT).show();

            final LinkedList<Move> moves = parser.makeMoveList(GlobalVars.moveSequence1);
            engine.setInitialGameState(moves);
            engine.newGame(postConfig, onGameStateReadyListener(postConfig, GlobalVars.movesCount1 + 4, listener));
        }
        else {
//            Toast.makeText(context, "movesCount = " + movesCount, Toast.LENGTH_SHORT).show();
//            Log.d("SYM777_DEBUG =====>", "movesCount = " + movesCount);

            engine.newGame(generatorConfig, onGameStateReadyListener(postConfig, movesCount, listener));
        }
    }

    private ZebraEngine.OnGameStateReadyListener onGameStateReadyListener(EngineConfig postConfig, int movesCountInput, OnGenerated listener) {
        return new ZebraEngine.OnGameStateReadyListener() {
            @Override
            public void onGameStateReady(GameState gameState) {
                GameStateListener waitForSettle = new GameStateListener() {

                    @Override
                    public void onBoard(GameState state) {

                        //validate if candidate moves are valid to current board
                        ByteBoard byteBoard = state.getByteBoard();
                        CandidateMove[] candidateMoves = state.getCandidateMoves();
                        for (CandidateMove candidateMove : candidateMoves) {
                            if (!byteBoard.isEmpty(candidateMove.getX(), candidateMove.getY())) {
                                return; // overlap - candidate moves are not loaded yet
                            }
                        }
                        if (candidateMoves.length <= 1) {
                            //skip needs to be counted as a move
                            return; //this move is forced or pass - let zebra play until some unforced move
                        }

                        returnGeneratedGame();

                    }

                    private void returnGeneratedGame() {
                            engine.updateConfig(gameState, postConfig);
                            gameState.setGameStateListener(null);
                            listener.onGenerated(gameState);

//                            Log.i("returnGeneratedGame", "*** " + GlobalVars.moveSequence0 + " * " + gameState.getMoveSequenceAsString() + " ***");
                    }

                    @Override
                    public void onGameOver() {
                        returnGeneratedGame();
                    }
                };

                GameStateListener waitForMovesCount = new GameStateListener() {

                    @Override
                    public void onBoard(GameState state) {
                        int discCount = state.getBlackPlayer().getDiscCount() + state.getWhitePlayer().getDiscCount();
//                        Log.i("BlackDisks = ", Integer.toString(state.getBlackPlayer().getDiscCount()));
//                        Log.i("WhiteDisks = ", Integer.toString(state.getWhitePlayer().getDiscCount()));
                        if (discCount == movesCountInput){
                            Log.i("final disccount", String.valueOf(discCount));
                            Log.i("movesCountInput = ", String.valueOf(movesCountInput));
                            gameState.setGameStateListener(waitForSettle);
                        }
                    }

                };


                gameState.setGameStateListener(waitForMovesCount);

            }
        };
    }

    public interface OnGenerated {
        void onGenerated(GameState gameState);
    }
}
