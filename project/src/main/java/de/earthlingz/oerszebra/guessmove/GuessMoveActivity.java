package de.earthlingz.oerszebra.guessmove;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.content.ContextCompat;

import com.innovattic.rangeseekbar.RangeSeekBar;
import com.shurik.droidzebra.EngineConfig;
import com.shurik.droidzebra.InvalidMove;
import com.shurik.droidzebra.ZebraEngine;
import de.earthlingz.oerszebra.AndroidContext;
import de.earthlingz.oerszebra.BoardView.BoardView;
import de.earthlingz.oerszebra.BoardView.BoardViewModel;
import de.earthlingz.oerszebra.GlobalSettingsLoader;
import de.earthlingz.oerszebra.R;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.shurik.droidzebra.ZebraEngine.PLAYER_BLACK;
import static de.earthlingz.oerszebra.GameSettingsConstants.BACKGROUND_COLOR_BLACK;
import static de.earthlingz.oerszebra.GameSettingsConstants.BACKGROUND_COLOR_GREEN;
import static de.earthlingz.oerszebra.GameSettingsConstants.BACKGROUND_COLOR_ORANGE;
import static de.earthlingz.oerszebra.guessmove.GuessMoveModeManager.GlobalVars.GameFromBad;
import static de.earthlingz.oerszebra.guessmove.GuessMoveModeManager.GlobalVars.db;

import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class GuessMoveActivity extends AppCompatActivity implements RangeSeekBar.SeekBarChangeListener {

    private BoardView boardView;
    private BoardViewModel boardViewModel;
    private GuessMoveModeManager manager;
    private ImageView sideToMoveCircle;
    private TextView hintText;

// 18.01.2020 SYM777:
    TextView DayStaticText;
    TextView GoodTextDay;
    TextView BadTextDay;
    TextView HintTextDay;
    TextView SumTextDay;
    TextView StatsTextDay;

    TextView IdTextMove;
    TextView GoodTextMove;
    TextView BadTextMove;
    TextView HintTextMove;
    TextView StatsTextMove;
    TextView EvalAvgTextMove;
    TextView EvalMinTextMove;

    LinearLayout MoveStatsFrame;
    GridLayout MoveStats;

    private static final String PREFS_NAME = "BDsettings";
    private static final String PREF_VIEW_RANGE = "ViewRange";  // 0 / 1 / 2 / 7 / 30 / -1
    private static final String PREF_RANGE_MIN = "RangeMin";
    private static final String PREF_RANGE_MAX = "RangeMax";
    SharedPreferences settings;

    static public String dateTextStartGuess;

    private Menu guess_move_context_menu;
// -----------------

    private EngineConfig engineConfig;
    private GlobalSettingsLoader globalSettingsLoader;

    DBHelper sqlHelper;

    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        globalSettingsLoader = new GlobalSettingsLoader(getApplicationContext());
        engineConfig = globalSettingsLoader.createEngineConfig();

        this.manager = new GuessMoveModeManager(ZebraEngine.get(
                new AndroidContext(getApplicationContext())),
                engineConfig, this);

        setContentView(R.layout.activity_guess_move);
        boardView = findViewById(R.id.guess_move_board);
        boardViewModel = manager;
        boardView.setBoardViewModel(boardViewModel);
        boardView.requestFocus();
        sideToMoveCircle = findViewById(R.id.side_to_move_circle);
        hintText = findViewById(R.id.guess_move_text);

// 15.10.2019 SYM777:
        GuessMoveModeManager.GlobalVars.GuessMode = true;
        GuessMoveModeManager.GlobalVars.GuessMoveActivityIsOn = true;

//        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setTitle(R.string.menu_item_guess_move);

        changeBackgroundColor();
// -----------------
// 18.01.2020 SYM777:
        DayStaticText = findViewById(R.id.DayStaticText);
        GoodTextDay = findViewById(R.id.GoodTextDay);
        BadTextDay = findViewById(R.id.BadTextDay);
        HintTextDay = findViewById(R.id.HintTextDay);
        SumTextDay = findViewById(R.id.SumTextDay);
        StatsTextDay = findViewById(R.id.StatsTextDay);

        IdTextMove = findViewById(R.id.IdTextMove);
        GoodTextMove = findViewById(R.id.GoodTextMove);
        BadTextMove = findViewById(R.id.BadTextMove);
        HintTextMove = findViewById(R.id.HintTextMove);
        StatsTextMove = findViewById(R.id.StatsTextMove);
        EvalAvgTextMove = findViewById(R.id.EvalAvgTextMove);
        EvalMinTextMove = findViewById(R.id.EvalMinTextMove);

        MoveStatsFrame = findViewById(R.id.MoveStatsFrame);
        MoveStats = findViewById(R.id.MoveStats);
// -----------------

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // 20.01.2020 SYM777: restore range settings
        RangeSeekBar range = findViewById(R.id.rangeSeekBar);
        range.setMinThumbValue(settings.getInt(PREF_RANGE_MIN, 4));
        range.setMaxThumbValue(settings.getInt(PREF_RANGE_MAX, 8));
//        Toast.makeText(this, settings.getInt(PREF_RANGE_MIN, 7) + " - " + settings.getInt(PREF_RANGE_MAX, 12), Toast.LENGTH_SHORT).show();
        TextView minText = findViewById(R.id.minText);
        minText.setText(String.valueOf(settings.getInt(PREF_RANGE_MIN, 4)));
        TextView maxText = findViewById(R.id.maxText);
        maxText.setText(String.valueOf(settings.getInt(PREF_RANGE_MAX, 8)));

//        Button button = findViewById(R.id.guess_move_new);
//        button.setOnClickListener((a) -> newGame(false));

        Date dateStartGuess = new Date();
// Форматирование времени как "день.месяц.год"
        DateFormat dateFormatStartGuess = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateTextStartGuess = dateFormatStartGuess.format(dateStartGuess);

// 13.01.2020 SYM777: add DataBase for guess stats
        sqlHelper = new DBHelper(this);
        GuessMoveModeManager.GlobalVars.db = sqlHelper.getWritableDatabase();

        newGame(false);

// 15.10.2019 SYM777: displaying statistics in Guess Mode
        setStatText();

        range.setSeekBarChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        setStatText();

        if (GuessMoveModeManager.GlobalVars.Restore) {
            GuessMoveModeManager.GlobalVars.Restore = false;
            newGame(true);
        }
        else {
            globalSettingsLoader.setOnSettingsChangedListener(() -> {
                this.engineConfig = globalSettingsLoader.createEngineConfig();
                this.manager.updateGlobalConfig(engineConfig);
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        globalSettingsLoader.setOnSettingsChangedListener(null);

    }

// 15.10.2019 SYM777:
    @Override
    protected void onDestroy() {
        GuessMoveModeManager.GlobalVars.GuessMode = false;
        GuessMoveModeManager.GlobalVars.GuessMoveActivityIsOn = false;

        GuessMoveModeManager.GlobalVars.db.close();

        super.onDestroy();
    }

// 16.10.2019 SYM777:
    private void changeBackgroundColor() {

        ActionBar actionBar = getSupportActionBar();

        if (GuessMoveModeManager.GlobalVars.BackgroundColor == BACKGROUND_COLOR_BLACK) {
            if (GuessMoveModeManager.GlobalVars.GuessMoveActivityIsOn) {
                if (actionBar != null) {
                    actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
                }
                RelativeLayout rl = findViewById(R.id.guess_move_main_frame);
                rl.setBackgroundColor(getResources().getColor(R.color.black));
                LinearLayout ll = findViewById(R.id.guess_move_board_info_layout);
                ll.setBackgroundColor(getResources().getColor(R.color.black));
//                Button b = findViewById(R.id.guess_move_new);
//                b.setBackgroundColor(getResources().getColor(R.color.black));
            }
        }
        if (GuessMoveModeManager.GlobalVars.BackgroundColor == BACKGROUND_COLOR_GREEN) {
            if (GuessMoveModeManager.GlobalVars.GuessMoveActivityIsOn) {
                if (actionBar != null) {
                    actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.board_color)));
                }
                RelativeLayout rl = findViewById(R.id.guess_move_main_frame);
                rl.setBackgroundColor(getResources().getColor(R.color.board_color));
                LinearLayout ll =  findViewById(R.id.guess_move_board_info_layout);
                ll.setBackgroundColor(getResources().getColor(R.color.board_color));
//                Button b = findViewById(R.id.guess_move_new);
//                b.setBackgroundColor(getResources().getColor(R.color.board_color));
            }
        }
        if (GuessMoveModeManager.GlobalVars.BackgroundColor == BACKGROUND_COLOR_ORANGE) {
            if (GuessMoveModeManager.GlobalVars.GuessMoveActivityIsOn) {
                if (actionBar != null) {
                    actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.orange)));
                }
                RelativeLayout rl = findViewById(R.id.guess_move_main_frame);
                rl.setBackgroundColor(getResources().getColor(R.color.orange));
                LinearLayout ll = findViewById(R.id.guess_move_board_info_layout);
                ll.setBackgroundColor(getResources().getColor(R.color.orange));
//                Button b = findViewById(R.id.guess_move_new);
//                b.setBackgroundColor(getResources().getColor(R.color.orange));
            }
        }
    }




    public void UnDoMove(View v) {
        manager.undoMove();
    }

    public void ReDoMove(View v) {
        manager.redoMove();
    }

    public void ShowHint(View v) {
        manager.hintGuess();
    }

    public void ShowDB(View v) {
        dbShow();
    }

    public void newGuessGame(View v) {
        newGame(false);
    }

    private void newGame(boolean ReLoad) {
        MoveStatsFrame.setBackgroundColor(Color.GRAY);
        MoveStatsFrame.setVisibility(INVISIBLE);
        MoveStats.setVisibility(INVISIBLE);

        GameFromBad = false;
        if (!ReLoad) {
            if (newGameFromBad()) ReLoad = true;
            Log.d("SYM777_DEBUG 3 -->", "Reload = " + ReLoad);
        }

        TextView minText = findViewById(R.id.minText);
        int min = Integer.valueOf(minText.getText().toString());
        TextView maxText = findViewById(R.id.maxText);
        int max = Integer.valueOf(maxText.getText().toString());
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Generating game");
        progressDialog.show();
        setBoardViewUnplayable();

// 15.10.2019 SYM777:
        GuessMoveModeManager.GlobalVars.GuessMode = true;

        manager.generate(min, max, new GuessMoveModeManager.GuessMoveListener() {
            @Override
            public void onGenerated(int sideToMove) {

                runOnUiThread(() -> {
                    boardView.setOnMakeMoveListener(move -> manager.guess(move));
                    updateSideToMoveCircle(sideToMove);
                    setGuessText(sideToMove);
                    progressDialog.dismiss();

                });

            }

            @Override
            public void onSideToMoveChanged(int sideToMove) {
                runOnUiThread(() -> {
                    updateSideToMoveCircle(sideToMove);
                    if (!guessed) {
                        setGuessText(sideToMove);
                    }
                });
            }

            private boolean guessed = false;

            @Override
            public void onCorrectGuess() {
                guessed = true;
                setHintText(Color.CYAN, R.string.guess_move_correct);
                if (GameFromBad) MoveStatsFrame.setBackgroundColor(Color.GREEN);
                else MoveStatsFrame.setBackgroundColor(Color.GRAY);
                setBoardViewPlayable();
            }

            @Override
            public void onBadGuess() {
                guessed = true;
                setHintText(Color.RED, R.string.guess_move_incorrect);
                if (GameFromBad) MoveStatsFrame.setBackgroundColor(Color.RED);
                else MoveStatsFrame.setBackgroundColor(Color.GRAY);
            }

// 22.01.2020 SYM777:
            @Override
            public void onHintGuess() {
                guessed = true;
                setHintText(Color.YELLOW, R.string.guess_hint_used);
                if (GameFromBad) MoveStatsFrame.setBackgroundColor(Color.YELLOW);
                else MoveStatsFrame.setBackgroundColor(Color.GRAY);
                setBoardViewPlayable();
            }
// -----------------

// 18.01.2020 SYM777:
            @Override
            public void onShowStats(String moveseq) {
                setMoveStatText(moveseq);
                setStatText();
            }
// -----------------

        }, ReLoad);
    }


    public boolean newGameFromBad() {
        String dateCondition = "";

        switch (settings.getInt(PREF_VIEW_RANGE, -1)) {
            case 0:
                dateCondition = ") BETWEEN '" + GuessMoveActivity.dateTextStartGuess + "' AND datetime('now','+3 hours')";
                break;
            case 1:
                dateCondition = ") BETWEEN datetime('now','+3 hours','start of day','-1 minutes') AND datetime('now','+3 hours')";
                break;
            case 2:
                dateCondition = ") BETWEEN datetime('now','+3 hours','start of day','-1 minutes','-1 days') AND datetime('now','+3 hours','start of day','-1 minutes')";
                break;
            case 7:
                dateCondition = ") BETWEEN datetime('now','+3 hours','start of day','-1 minutes','-7 days','weekday 1') AND datetime('now','+3 hours')";
                break;
            case 30:
                dateCondition = ") BETWEEN datetime('now','+3 hours','start of month','-1 minutes') AND datetime('now','+3 hours')";
                break;
            case -1:
                dateCondition = ") BETWEEN datetime('now','+3 hours','start of year','-100 years') AND datetime('now','+3 hours')";
                break;
            default:
                dateCondition = ") BETWEEN datetime('now','+3 hours','start of year','-100 years') AND datetime('now','+3 hours')";
        }


        String sortCondition = "bad2 DESC, eval_min ASC, eval ASC, date_text DESC, time_text DESC";

        StringBuilder sqlQuery_builder = new StringBuilder();

        sqlQuery_builder.append("SELECT ").append(DBHelper.COLUMN_ID).append(", ")
                .append("strftime('%d.%m.%Y', MAX(").append(DBHelper.COLUMN_STR_DATE).append("))").append(" date_text").append(", ")
                .append("strftime('%H:%M:%S', MAX(").append(DBHelper.COLUMN_STR_DATE).append("))").append(" time_text").append(", ")
                .append(DBHelper.COLUMN_INT_MOVE).append(", ")
                .append(DBHelper.COLUMN_STR_MOVESEQ).append(", ")
                .append("SUM(").append(DBHelper.COLUMN_INT_GOOD).append(")").append(" good").append(", ")
                .append("SUM(").append(DBHelper.COLUMN_INT_BAD).append(")").append(" bad2").append(", ")
                .append("SUM(").append(DBHelper.COLUMN_INT_HINT).append(")").append(" hint2").append(", ")
                .append("CASE WHEN SUM(bad) != '0' THEN ROUND(AVG(eval)*(SUM(good)+SUM(bad)+SUM(hint))/SUM(bad), 0) ELSE ROUND(AVG(eval), 0) END").append(" eval").append(", ")
                .append("MIN(").append(DBHelper.COLUMN_INT_EVALMIN).append(")").append(" eval_min").append(", ")
                .append(DBHelper.COLUMN_STR_NOTE)
                .append(" FROM ").append(DBHelper.TABLE_GUESS)
                .append(" WHERE datetime(").append(DBHelper.COLUMN_STR_DATE).append(dateCondition)
                .append(" GROUP BY ").append(DBHelper.COLUMN_STR_MOVESEQ)
                .append(" HAVING bad2 > 1 OR hint2 > 1")
                .append(" ORDER BY ").append(sortCondition);

        String sqlQuery = sqlQuery_builder.toString().replace("null", "");

        Log.d("SYM777_DEBUG 1 -->", sqlQuery);

        //получаем данные из бд в виде курсора
        Cursor userCursor = db.rawQuery(sqlQuery, null);

        userCursor.moveToFirst();

        Log.d("SYM777_DEBUG 2 -->", "count = " + userCursor.getCount());

        if (userCursor.getCount() == 0) {
            userCursor.close();
            return false;
        }

        int EvalMinSum = 0;

        while (!userCursor.isAfterLast()) {

            try {
                if (userCursor.getInt(userCursor.getColumnIndex(DBHelper.COLUMN_INT_EVALMIN)) < 0)
                    EvalMinSum += userCursor.getInt(userCursor.getColumnIndex(DBHelper.COLUMN_INT_EVALMIN));
                if (userCursor.getLong(userCursor.getColumnIndex("hint2")) > 1)
                    EvalMinSum += -5*userCursor.getLong(userCursor.getColumnIndex("hint2"));
            } catch (NumberFormatException e) {
                System.err.println("Неверный формат строки!");
            }

            userCursor.moveToNext();
        }

        Log.d("SYM777_DEBUG 3 -->", "EvalMinSum = " + EvalMinSum);

        if (EvalMinSum == 0) {
            userCursor.close();
            return false;
        }

        int NotRepeat = EvalMinSum * 4;
        EvalMinSum += NotRepeat;
        int Rand = random.nextInt(Math.abs(EvalMinSum)) + 1;

        Log.d("SYM777_DEBUG 4 -->", "Rand = " + Rand + ", EvalMinSum* = " + EvalMinSum);

        long Id = 0;
        int EvalMin = 0;
        long Hint = 0;
        int Move = 0;
        String Seq = "";

        while (Rand > 0) {

            Rand -= Math.abs(NotRepeat);
            if (Rand <= 0) {
                Id = 0;
                break;
            }

            userCursor.moveToFirst();

            while (!userCursor.isAfterLast()) {
                try {
                    Id = userCursor.getLong(userCursor.getColumnIndex(DBHelper.COLUMN_ID));
                    EvalMin = userCursor.getInt(userCursor.getColumnIndex(DBHelper.COLUMN_INT_EVALMIN));
                    Hint = userCursor.getLong(userCursor.getColumnIndex("hint2"));

                    if (EvalMin < 0)
                        Rand -= Math.abs(EvalMin);

                    if ( Hint > 1)
                        Rand -= 5*Hint;

                    if (Rand <= 0) {
                        if (userCursor.getInt(3) > 0) {
                            GuessMoveModeManager.GlobalVars.movesCount1 = userCursor.getInt(3);
                            GuessMoveModeManager.GlobalVars.moveSequence1 = userCursor.getString(4);
                            Move = userCursor.getInt(3);
                            Seq = userCursor.getString(4);
                        }

                        userCursor.moveToLast();
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Неверный формат строки!");
                }

                userCursor.moveToNext();
            }
        }

        userCursor.close();

        Log.d("SYM777_DEBUG -->", "Id = " + Id + " (Move = " + Move + ", Seq = " + Seq + ")");

        if (Id != 0) {
//            DayStaticText.setTextColor(Color.MAGENTA);
            GameFromBad = true;
            return true;
        }
        else {
//            DayStaticText.setTextColor(Color.GRAY);
            GameFromBad = false;
            return false;
        }
    }



// =============================================================================
// 20.01.2020 SYM777: displaying statistics in Guess Mode
// =============================================================================

    public void setMoveStatText(String moveseq)
    {
        String dateCondition = "";

        switch (settings.getInt(PREF_VIEW_RANGE, -1)) {
            case 0:
                dateCondition = ") BETWEEN '" + GuessMoveActivity.dateTextStartGuess + "' AND datetime('now','+3 hours')";
                break;
            case 1:
                dateCondition = ") BETWEEN datetime('now','+3 hours','start of day','-1 minutes') AND datetime('now','+3 hours')";
                break;
            case 2:
                dateCondition = ") BETWEEN datetime('now','+3 hours','start of day','-1 minutes','-1 days') AND datetime('now','+3 hours','start of day','-1 minutes')";
                break;
            case 7:
                dateCondition = ") BETWEEN datetime('now','+3 hours','start of day','-1 minutes','-7 days','weekday 1') AND datetime('now','+3 hours')";
                break;
            case 30:
                dateCondition = ") BETWEEN datetime('now','+3 hours','start of month','-1 minutes') AND datetime('now','+3 hours')";
                break;
            case -1:
                dateCondition = ") BETWEEN datetime('now','+3 hours','start of year','-100 years') AND datetime('now','+3 hours')";
                break;
            default:
                dateCondition = ") BETWEEN datetime('now','+3 hours','start of year','-100 years') AND datetime('now','+3 hours')";
        }

        StringBuilder sqlQuery_builder = new StringBuilder();

        sqlQuery_builder.append("SELECT ").append(DBHelper.COLUMN_ID).append(", ")
                .append(DBHelper.COLUMN_STR_DATE).append(", ")
                .append(DBHelper.COLUMN_STR_MOVESEQ).append(", ")
                .append("SUM(").append(DBHelper.COLUMN_INT_GOOD).append(")").append(" good").append(", ")
                .append("SUM(").append(DBHelper.COLUMN_INT_BAD).append(")").append(" bad").append(", ")
                .append("SUM(").append(DBHelper.COLUMN_INT_HINT).append(")").append(" hint").append(", ")
                .append("CASE WHEN SUM(bad) != '0' THEN ROUND(AVG(eval)*(SUM(good)+SUM(bad)+SUM(hint))/SUM(bad), 0) ELSE ROUND(AVG(eval), 0) END").append(" eval").append(", ")
                .append("MIN(").append(DBHelper.COLUMN_INT_EVALMIN).append(")").append(" eval_min")//.append(", ")
                .append(" FROM ").append(DBHelper.TABLE_GUESS)
//                .append(" WHERE date(").append(DBHelper.COLUMN_STR_DATE).append(") = date('now','+3 hours') AND ").append(DBHelper.COLUMN_STR_MOVESEQ).append(" = '").append(moveseq).append("'")
                .append(" WHERE datetime(").append(DBHelper.COLUMN_STR_DATE).append(dateCondition).append(" AND ").append(DBHelper.COLUMN_STR_MOVESEQ).append(" = '").append(moveseq).append("'")
                .append(" GROUP BY ").append(DBHelper.COLUMN_STR_MOVESEQ)
                .append(" ORDER BY ").append(DBHelper.COLUMN_ID).append(" ASC");

        String sqlQuery = sqlQuery_builder.toString().replace("null", "");

        Log.d("SYM777_DEBUG -->", sqlQuery);

        Cursor userCursor = db.rawQuery(sqlQuery, null);

        userCursor.moveToFirst();

        long Id = 0;
        long GoodDay = 0;
        long BadDay = 0;
        long HintDay = 0;
        int EvalMove = 0;
        int EvalMinMove = 0;

        while (!userCursor.isAfterLast())
        {
            try {
                Id = Long.valueOf(userCursor.getString(userCursor.getColumnIndex(DBHelper.COLUMN_ID)));
                GoodDay += Long.valueOf(userCursor.getString(userCursor.getColumnIndex(DBHelper.COLUMN_INT_GOOD)));
                BadDay += Long.valueOf(userCursor.getString(userCursor.getColumnIndex(DBHelper.COLUMN_INT_BAD)));
                HintDay += Long.valueOf(userCursor.getString(userCursor.getColumnIndex(DBHelper.COLUMN_INT_HINT)));
                EvalMove += Long.valueOf(userCursor.getString(userCursor.getColumnIndex(DBHelper.COLUMN_INT_EVAL)));
                EvalMinMove += Long.valueOf(userCursor.getString(userCursor.getColumnIndex(DBHelper.COLUMN_INT_EVALMIN)));
            } catch (NumberFormatException e) {
                System.err.println("Неверный формат строки!");
            }

            userCursor.moveToNext();
        }

        userCursor.close();

        long StDay = -1;

        if((GoodDay > 0) || (BadDay > 0) || (HintDay > 0))
            StDay = (long)(100.0 * (float)GoodDay / (float)(GoodDay + BadDay + HintDay));


        long StDay2 = StDay;

        long Id2 = Id;
        long GoodDay2 = GoodDay;
        long BadDay2 = BadDay;
        long HintDay2 = HintDay;
        int EvalMove2 = EvalMove;
        int EvalMinMove2 = EvalMinMove;

        Handler handler = new Handler(getBaseContext().getMainLooper());
        handler.post( new Runnable() {
            @Override
            public void run() {
//                if (GameFromBad) MoveStatsFrame.setBackgroundColor(Color.MAGENTA);
//                else MoveStatsFrame.setBackgroundColor(Color.GRAY);
                MoveStatsFrame.setVisibility(VISIBLE);
                MoveStats.setVisibility(VISIBLE);
                IdTextMove.setText(String.format(Locale.ENGLISH, "%d", Id2));
                GoodTextMove.setText(String.format(Locale.ENGLISH, "%d", GoodDay2));
                BadTextMove.setText(String.format(Locale.ENGLISH, "%d", BadDay2));
                HintTextMove.setText(String.format(Locale.ENGLISH, "%d", HintDay2));
//                SumTextMove.setText(String.format(Locale.ENGLISH, "%d", (GoodDay2 + BadDay2 + HintDay2)));
                EvalAvgTextMove.setText(String.format(Locale.ENGLISH, "%d", EvalMove2));
                EvalMinTextMove.setText(String.format(Locale.ENGLISH, "( %d )", EvalMinMove2));

                if (StDay2 >= 0) {
                    StatsTextMove.setText(String.format(Locale.ENGLISH, "%d %%", StDay2));
                    if (StDay2 > 50) StatsTextMove.setTextColor(Color.GREEN);
                    else if (StDay2 < 50) StatsTextMove.setTextColor(Color.RED);
                    else StatsTextMove.setTextColor(Color.YELLOW);
                } else {
                    StatsTextMove.setText(" ");
                    StatsTextMove.setTextColor(Color.GRAY);
                }
            }
        } );

    }

// ------------------------------------------------------------------------

public void setStatText()
{
    String dateCondition = "";

    switch (settings.getInt(PREF_VIEW_RANGE, -1)) {
        case 0:
            DayStaticText.setText(R.string.static_session);
            dateCondition = ") BETWEEN '" + GuessMoveActivity.dateTextStartGuess + "' AND datetime('now','+3 hours')";
            break;
        case 1:
            DayStaticText.setText(R.string.static_today);
            dateCondition = ") BETWEEN datetime('now','+3 hours','start of day','-1 minutes') AND datetime('now','+3 hours')";
            break;
        case 2:
            DayStaticText.setText(R.string.static_yesterday);
            dateCondition = ") BETWEEN datetime('now','+3 hours','start of day','-1 minutes','-1 days') AND datetime('now','+3 hours','start of day','-1 minutes')";
            break;
        case 7:
            DayStaticText.setText(R.string.static_week);
            dateCondition = ") BETWEEN datetime('now','+3 hours','start of day','-1 minutes','-7 days','weekday 1') AND datetime('now','+3 hours')";
            break;
        case 30:
            DayStaticText.setText(R.string.static_month);
            dateCondition = ") BETWEEN datetime('now','+3 hours','start of month','-1 minutes') AND datetime('now','+3 hours')";
            break;
        case -1:
            DayStaticText.setText(R.string.static_all);
            dateCondition = ") BETWEEN datetime('now','+3 hours','start of year','-100 years') AND datetime('now','+3 hours')";
            break;
        default:
            DayStaticText.setText(R.string.static_all);
            dateCondition = ") BETWEEN datetime('now','+3 hours','start of year','-100 years') AND datetime('now','+3 hours')";
    }

    StringBuilder sqlQuery_builder = new StringBuilder();

    sqlQuery_builder.append("SELECT ").append(DBHelper.COLUMN_ID).append(", ")
            .append("MAX(strftime('%d.%m.%Y', ").append(DBHelper.COLUMN_STR_DATE).append("))").append(" date_text").append(", ")
            .append(DBHelper.COLUMN_STR_MOVESEQ).append(", ")
            .append("SUM(").append(DBHelper.COLUMN_INT_GOOD).append(")").append(" good").append(", ")
            .append("SUM(").append(DBHelper.COLUMN_INT_BAD).append(")").append(" bad").append(", ")
            .append("SUM(").append(DBHelper.COLUMN_INT_HINT).append(")").append(" hint")//.append(", ")
            .append(" FROM ").append(DBHelper.TABLE_GUESS)
            .append(" WHERE datetime(").append(DBHelper.COLUMN_STR_DATE).append(dateCondition)
            .append(" GROUP BY ").append(DBHelper.COLUMN_STR_MOVESEQ)
            .append(" ORDER BY ").append(DBHelper.COLUMN_ID).append(" ASC");

    String sqlQuery = sqlQuery_builder.toString().replace("null", "");

    Log.d("SYM777_DEBUG -->", sqlQuery);

//    Cursor userCursor = db.rawQuery("select * from "+ DBHelper.TABLE_DAILY + " ORDER BY " + "\"_id\"" + " ASC", null);
    Cursor userCursor = db.rawQuery(sqlQuery, null);

    userCursor.moveToFirst();

    long GoodDay = 0;
    long BadDay = 0;
    long HintDay = 0;

    while (!userCursor.isAfterLast())
    {
        try {
            GoodDay += Long.valueOf(userCursor.getString(userCursor.getColumnIndex(DBHelper.COLUMN_INT_GOOD)));
            BadDay += Long.valueOf(userCursor.getString(userCursor.getColumnIndex(DBHelper.COLUMN_INT_BAD)));
            HintDay += Long.valueOf(userCursor.getString(userCursor.getColumnIndex(DBHelper.COLUMN_INT_HINT)));
        } catch (NumberFormatException e) {
            System.err.println("Неверный формат строки!");
        }

        userCursor.moveToNext();
    }

    userCursor.close();

    long StDay = -1;

    if((GoodDay > 0) || (BadDay > 0) || (HintDay > 0))
        StDay = (long)(100.0 * (float)GoodDay / (float)(GoodDay + BadDay + HintDay));


    long StDay2 = StDay;

    long GoodDay2 = GoodDay;
    long BadDay2 = BadDay;
    long HintDay2 = HintDay;

    Handler handler = new Handler(getBaseContext().getMainLooper());
    handler.post( new Runnable() {
        @Override
        public void run() {
            GoodTextDay.setText(String.format(Locale.ENGLISH, "%d", GoodDay2));
            BadTextDay.setText(String.format(Locale.ENGLISH, "%d", BadDay2));
            HintTextDay.setText(String.format(Locale.ENGLISH, "%d", HintDay2));
            SumTextDay.setText(String.format(Locale.ENGLISH, "%d", (GoodDay2 + BadDay2 + HintDay2)));

            if (StDay2 >= 0) {
                StatsTextDay.setText(String.format(Locale.ENGLISH, "%d %%", StDay2));
                if (StDay2 > 50) StatsTextDay.setTextColor(Color.GREEN);
                else if (StDay2 < 50) StatsTextDay.setTextColor(Color.RED);
                else StatsTextDay.setTextColor(Color.YELLOW);
            } else {
                StatsTextDay.setText(" ");
                StatsTextDay.setTextColor(Color.GRAY);
            }
        }
    } );
}


// =============================================================================



    private void setBoardViewUnplayable() {
        boardView.setOnMakeMoveListener(null);

        boardView.setDisplayMoves(false);
        boardView.setDisplayEvals(true);
    }

    private void setBoardViewPlayable() {
        boardView.setDisplayMoves(true);
        boardView.setDisplayEvals(true);
        boardView.setDisplayAnimations(true);
        boardView.setAnimationDuration(500);
        boardView.setOnMakeMoveListener(move1 -> {
            try {
                manager.move(move1);
            } catch (InvalidMove ignored) {
            }
        });
    }

    private void setHintText(int cyan, int guess_move_correct) {
        hintText.setTextColor(cyan);
        hintText.setText(guess_move_correct);
    }

    private void setGuessText(int sideToMove) {
        if (sideToMove == PLAYER_BLACK) {
            hintText.setText(R.string.guess_black_move_hint);
            hintText.setTextColor(Color.BLACK);
        } else {
            hintText.setText(R.string.guess_white_move_hint);
            hintText.setTextColor(Color.WHITE);
        }

    }

    private void updateSideToMoveCircle(int sideToMove) {
        if (sideToMove == PLAYER_BLACK) {
            sideToMoveCircle.setImageResource(R.drawable.black_circle);
        } else {
            sideToMoveCircle.setImageResource(R.drawable.white_circle);
        }
    }

    /* Creates the menu items */
    @Override
    @SuppressLint("RestrictedApi")
    public boolean onCreateOptionsMenu(Menu menu) {
        this.guess_move_context_menu = menu;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.guess_move_context_menu, menu);

        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_new_game:
                newGame(false);
                return true;

// 15.10.2019 SYM777: подсказка в режие GuessMove
            case R.id.menu_hint:
                manager.hintGuess();
                return true;

            case R.id.menu_take_back:
                manager.undoMove();
                return true;
            case R.id.menu_take_redo:
                manager.redoMove();
                return true;

            case R.id.menu_showdb:
                dbShow();
                return true;
        }
        return false;
    }

    @Override
    public void onStartedSeeking() {

    }

    @Override
    public void onStoppedSeeking() {
        RangeSeekBar range = findViewById(R.id.rangeSeekBar);
        SharedPreferences.Editor prefEditor = settings.edit();
        prefEditor.putInt(PREF_RANGE_MIN, range.getMinThumbValue());
        prefEditor.putInt(PREF_RANGE_MAX, range.getMaxThumbValue());
        prefEditor.apply();
//        Toast toast = Toast.makeText(this, range.getMinThumbValue() + " - " + range.getMaxThumbValue(), Toast.LENGTH_SHORT);
//        toast.show();
    }

    @Override
    public void onValueChanged(int min, int max) {
        TextView minText = findViewById(R.id.minText);
        minText.setText(String.valueOf(min));
        TextView maxText = findViewById(R.id.maxText);
        maxText.setText(String.valueOf(max));
// 15.10.2019 SYM777: displaying statistics in Guess Mode
//        GuessMoveModeManager.GlobalVars.CounterOfGoodLevel = 0;
//        GuessMoveModeManager.GlobalVars.CounterOfBadLevel = 0;
        setStatText();
// -----------------
    }


    public void dbShow(){
        Intent intent = new Intent(this, DataBaseActivity.class);
        startActivity(intent);
    }
}
