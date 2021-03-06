package com.shurik.droidzebra;

public class EngineConfig {
    public final int engineFunction;
    public final int engineBackgroundColor;

    public final int depth;
    public final int depthExact;
    public final int depthWLD;

    public final boolean autoForcedMoves;
    public final String forcedOpening;
    public final boolean humanOpenings;
    public final boolean practiceMode;
    public final boolean useBook;
    public final int slack;
    public final int perturbation;
    public final int computerMoveDelay;

    public EngineConfig(int engineFunction, int depth, int depthExact, int depthWLD, boolean autoForcedMoves, String forcedOpening, boolean humanOpenings, boolean practiceMode, boolean useBook, int slack, int perturbation, int computerMoveDelay, int engineBackgroundColor) {
        this.engineFunction = engineFunction;
        this.depth = depth;
        this.depthExact = depthExact;
        this.depthWLD = depthWLD;
        this.autoForcedMoves = autoForcedMoves;
        this.forcedOpening = forcedOpening;
        this.humanOpenings = humanOpenings;
        this.practiceMode = practiceMode;
        this.useBook = useBook;
        this.slack = slack;
        this.perturbation = perturbation;
        this.computerMoveDelay = computerMoveDelay;
        this.engineBackgroundColor = engineBackgroundColor;
    }

    public EngineConfig alterPracticeMode(boolean practiceMode) {
        return new EngineConfig(engineFunction, depth, depthExact, depthWLD, autoForcedMoves, forcedOpening, humanOpenings, practiceMode, useBook, slack, perturbation, computerMoveDelay, engineBackgroundColor);
    }
}
