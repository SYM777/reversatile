package de.earthlingz.oerszebra.guessmove;

import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "datastore.db"; // название бд
    private static final int DATABASE_VERSION = 1; // версия базы данных
    static final String TABLE_GUESS = "data_guess"; // название таблицы в бд
    public static final String TABLE_GAME_SAVES = "data_game_saves"; // название таблицы в бд

    // названия столбцов
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_STR_DATE = "date_text";
    static final String COLUMN_INT_MOVE = "move";        // номер хода
    public static final String COLUMN_STR_MOVESEQ = "move_seq";  // последовательность ходов
    static final String COLUMN_INT_GOOD = "good";        // число успешных попыток
    static final String COLUMN_INT_BAD = "bad";          // число неуспешных попыток
    static final String COLUMN_INT_HINT = "hint";        // число посдказок
    static final String COLUMN_INT_EVAL = "eval";        // средний коэффициент неверных ответов
    static final String COLUMN_INT_EVALMIN = "eval_min";  // наихудший коэффициент неверных ответов
    public static final String COLUMN_STR_NOTE = "note";        // примечание
    public static final String COLUMN_STR_BPLAYER = "black_player";        // Имя игрока черными
    public static final String COLUMN_STR_WPLAYER = "white_player";        // Имя игрока белыми
    public static final String COLUMN_INT_BDISCS = "black_discs";        // Число черных дисков
    public static final String COLUMN_INT_WDISCS = "white_discs";        // Число белых дисков

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE data_guess (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_STR_DATE + " TEXT, " +
                COLUMN_INT_MOVE + " INTEGER, " +
                COLUMN_STR_MOVESEQ + " TEXT, " +
                COLUMN_INT_GOOD + " INTEGER, " +
                COLUMN_INT_BAD + " INTEGER, " +
                COLUMN_INT_HINT + " INTEGER, " +
                COLUMN_INT_EVAL + " INTEGER, " +
                COLUMN_INT_EVALMIN + " INTEGER, " +
                COLUMN_STR_NOTE + " TEXT);");

        db.execSQL("CREATE TABLE data_game_saves (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_STR_DATE + " TEXT, " +
                COLUMN_STR_MOVESEQ + " TEXT, " +
                COLUMN_STR_BPLAYER + " TEXT, " +
                COLUMN_STR_WPLAYER + " TEXT, " +
                COLUMN_INT_BDISCS + " TEXT, " +
                COLUMN_INT_WDISCS + " TEXT, " +
                COLUMN_STR_NOTE + " TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,  int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_GUESS);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_GAME_SAVES);
        onCreate(db);
    }
}