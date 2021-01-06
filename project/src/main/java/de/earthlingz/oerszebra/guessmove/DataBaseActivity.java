package de.earthlingz.oerszebra.guessmove;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import de.earthlingz.oerszebra.R;

import static android.view.View.VISIBLE;

public class DataBaseActivity extends AppCompatActivity {

    ListView userList;
    DBHelper databaseHelper;
    SQLiteDatabase db;
    Cursor userCursor;
    SimpleCursorAdapter userAdapter;

    private static final String PREFS_NAME = "BDsettings";
    private static final String PREF_VIEW_RANGE = "ViewRange";  // 0 / 1 / 2 / 7 / 30 / -1 /
    private static final String PREF_SORT = "PrefSort";     // 0  1    2     3   4    5   6
                                                            // id date moves seq good bad hint
    private static final String PREF_SORT_DIR = "PrefSortDir";  // 0 / 1
    SharedPreferences settings;

    private Menu guess_bd_context_menu;

    Button DBidSort;
    Button DBdateSort;
    Button DBmovesSort;
    Button DBseqSort;
    Button DBgoodSort;
    Button DBbadSort;
    Button DBhintSort;

    TextView GoodText;
    TextView BadText;
    TextView HintText;
    TextView StatsText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        userList = (ListView)findViewById(R.id.list);

        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
// получаем элемент по id из БД
                userCursor = db.rawQuery("select * from " + DBHelper.TABLE_GUESS + " where " +
                            DBHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
                userCursor.moveToFirst();

                if (userCursor.getInt(2) > 0) {
                    GuessMoveModeManager.GlobalVars.movesCount1 = userCursor.getInt(2);
                    GuessMoveModeManager.GlobalVars.moveSequence1 = userCursor.getString(3);
                    GuessMoveModeManager.GlobalVars.Restore = true;
                }

                userCursor.close();

                goHome();
            }
        });

        DBidSort = findViewById(R.id.DBidSort);
        DBdateSort = findViewById(R.id.DBdateSort);
        DBmovesSort = findViewById(R.id.DBmoveSort);
        DBseqSort = findViewById(R.id.DBseqSort);
        DBgoodSort = findViewById(R.id.DBgoodSort);
        DBbadSort = findViewById(R.id.DBbadSort);
        DBhintSort = findViewById(R.id.DBhintSort);

        GoodText = findViewById(R.id.GoodText);
        BadText = findViewById(R.id.BadText);
        HintText = findViewById(R.id.HintText);
        StatsText = findViewById(R.id.StatsText);

        databaseHelper = new DBHelper(this);
    }

    private void goHome(){
        // закрываем подключение
        db.close();
        // переход к главной activity
        Intent intent = new Intent(this, GuessMoveActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }


    @Override
    public void onResume() {
        super.onResume();

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


        String sortCondition = "_id ASC";

        DBidSort.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
        DBdateSort.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
        DBmovesSort.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
        DBseqSort.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
        DBgoodSort.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
        DBbadSort.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
        DBhintSort.setBackgroundColor(ContextCompat.getColor(this, R.color.black));

        switch (settings.getInt(PREF_SORT, 0)) {
            case 0:
                sortCondition = "_id ";
                if (settings.getInt(PREF_SORT_DIR, 0) == 0) {sortCondition += "ASC";} else {sortCondition += "DESC";}
                DBidSort.setBackgroundColor(ContextCompat.getColor(this, R.color.board_color));
                break;
            case 1:
                sortCondition = "date_text ";
                if (settings.getInt(PREF_SORT_DIR, 0) == 0) {sortCondition += "ASC";} else {sortCondition += "DESC";}
//                sortCondition += ", time_text ";
//                if (settings.getInt(PREF_SORT_DIR, 0) == 0) {sortCondition += "ASC";} else {sortCondition += "DESC";}
                DBdateSort.setBackgroundColor(ContextCompat.getColor(this, R.color.board_color));
                break;
            case 2:
                sortCondition = "move ";
                if (settings.getInt(PREF_SORT_DIR, 0) == 0) {sortCondition += "ASC";} else {sortCondition += "DESC";}
                sortCondition += ", move_seq "; sortCondition += "ASC";
                DBmovesSort.setBackgroundColor(ContextCompat.getColor(this, R.color.board_color));
                break;
            case 3:
                sortCondition = "move_seq ";
                if (settings.getInt(PREF_SORT_DIR, 0) == 0) {sortCondition += "ASC";} else {sortCondition += "DESC";}
                DBseqSort.setBackgroundColor(ContextCompat.getColor(this, R.color.board_color));
                break;
            case 4:
                sortCondition = "good ";
                if (settings.getInt(PREF_SORT_DIR, 0) == 0) {sortCondition += "ASC";} else {sortCondition += "DESC";}
                sortCondition += ", date_text "; sortCondition += "DESC";
//                sortCondition += ", time_text "; sortCondition += "DESC";
                DBgoodSort.setBackgroundColor(ContextCompat.getColor(this, R.color.board_color));
                break;
            case 5:
                sortCondition = "bad ";
                if (settings.getInt(PREF_SORT_DIR, 0) == 0) {sortCondition += "ASC";} else {sortCondition += "DESC";}
                sortCondition += ", eval_min "; sortCondition += "ASC";
                sortCondition += ", eval "; sortCondition += "ASC";
                sortCondition += ", date_text "; sortCondition += "DESC";
//                sortCondition += ", time_text "; sortCondition += "DESC";
                DBbadSort.setBackgroundColor(ContextCompat.getColor(this, R.color.board_color));
                break;
            case 6:
                sortCondition = "hint ";
                if (settings.getInt(PREF_SORT_DIR, 0) == 0) {sortCondition += "ASC";} else {sortCondition += "DESC";}
                sortCondition += ", date_text "; sortCondition += "DESC";
//                sortCondition += ", time_text "; sortCondition += "DESC";
                DBhintSort.setBackgroundColor(ContextCompat.getColor(this, R.color.board_color));
                break;
            default:
                sortCondition = "_id ";
                if (settings.getInt(PREF_SORT_DIR, 0) == 0) {sortCondition += "ASC";} else {sortCondition += "DESC";}
                DBidSort.setBackgroundColor(ContextCompat.getColor(this, R.color.board_color));
        }


                // открываем подключение
        db = databaseHelper.getReadableDatabase();

        StringBuilder sqlQuery_builder = new StringBuilder();

        sqlQuery_builder.append("SELECT ").append(DBHelper.COLUMN_ID).append(", ")
                .append("strftime('%d.%m.%Y', MAX(").append(DBHelper.COLUMN_STR_DATE).append("))").append(" date_text1").append(", ")
                .append("strftime('%H:%M:%S', MAX(").append(DBHelper.COLUMN_STR_DATE).append("))").append(" time_text").append(", ")
                .append(DBHelper.COLUMN_INT_MOVE).append(", ")
                .append(DBHelper.COLUMN_STR_MOVESEQ).append(", ")
                .append("SUM(").append(DBHelper.COLUMN_INT_GOOD).append(")").append(" good").append(", ")
                .append("SUM(").append(DBHelper.COLUMN_INT_BAD).append(")").append(" bad").append(", ")
                .append("SUM(").append(DBHelper.COLUMN_INT_HINT).append(")").append(" hint").append(", ")
//                .append("ROUND(AVG(").append(DBHelper.COLUMN_INT_EVAL).append("), 0)").append(" eval").append(", ")
                .append("CASE WHEN SUM(bad) != '0' THEN ROUND(AVG(eval)*(SUM(good)+SUM(bad)+SUM(hint))/SUM(bad), 0) ELSE ROUND(AVG(eval), 0) END").append(" eval").append(", ")
//                .append("CASE WHEN bad != '0' THEN ROUND(AVG(").append(DBHelper.COLUMN_INT_EVAL).append("), 0) END").append(" eval").append(", ")
                .append("MIN(").append(DBHelper.COLUMN_INT_EVALMIN).append(")").append(" eval_min").append(", ")
                .append(DBHelper.COLUMN_STR_NOTE)
                .append(" FROM ").append(DBHelper.TABLE_GUESS)
                .append(" WHERE datetime(").append(DBHelper.COLUMN_STR_DATE).append(dateCondition)
                .append(" GROUP BY ").append(DBHelper.COLUMN_STR_MOVESEQ)
//                .append(" ORDER BY ").append(DBHelper.COLUMN_ID).append(" DESC");
                .append(" ORDER BY ").append(sortCondition);

        String sqlQuery = sqlQuery_builder.toString().replace("null", "");

        Log.d("SYM777_DEBUG -->", sqlQuery);

        //получаем данные из бд в виде курсора
//        userCursor = db.rawQuery("select * from "+ DBHelper.TABLE_DAILY + " ORDER BY " + "\"_id\"" + " DESC", null);
        userCursor = db.rawQuery(sqlQuery, null);
        // определяем, какие столбцы из курсора будут выводиться в ListView
        String[] headers = new String[] {DBHelper.COLUMN_ID, "date_text1", "time_text", DBHelper.COLUMN_INT_MOVE, DBHelper.COLUMN_STR_MOVESEQ,
                                        DBHelper.COLUMN_INT_GOOD, DBHelper.COLUMN_INT_BAD, DBHelper.COLUMN_INT_HINT,
                                        DBHelper.COLUMN_INT_EVAL, DBHelper.COLUMN_INT_EVALMIN, DBHelper.COLUMN_STR_NOTE};
        // создаем адаптер, передаем в него курсор
        userAdapter = new SimpleCursorAdapter(this, R.layout.list_item,
                userCursor, headers, new int[]{R.id.text_id, R.id.text_date, R.id.text_time, R.id.text_move, R.id.text_moveseq,
                                               R.id.text_good, R.id.text_bad, R.id.text_hint,
                                               R.id.text_eval, R.id.text_evalmax, R.id.text_note}, 0);
        userList.setAdapter(userAdapter);


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
                GoodText.setText(String.format(Locale.ENGLISH, "\u03A3 = %d", GoodDay2));
                BadText.setText(String.format(Locale.ENGLISH, "\u03A3 = %d", BadDay2));
                HintText.setText(String.format(Locale.ENGLISH, "\u03A3 = %d", HintDay2));

                if (StDay2 >= 0) {
                    StatsText.setText(String.format(Locale.ENGLISH, "%d %%", StDay2));
                    if (StDay2 > 50) StatsText.setTextColor(Color.GREEN);
                    else if (StDay2 < 50) StatsText.setTextColor(Color.RED);
                    else StatsText.setTextColor(Color.YELLOW);
                } else {
                    StatsText.setText(" ");
                    StatsText.setTextColor(Color.GRAY);
                }
            }
        } );

    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        // Закрываем подключение и курсор
        db.close();
        userCursor.close();
    }


    /* Creates the menu items */
    @Override
    @SuppressLint("RestrictedApi")
    public boolean onCreateOptionsMenu(Menu menu) {
        this.guess_bd_context_menu = menu;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.guess_bd_context_menu, menu);

        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        if (settings.getInt(PREF_VIEW_RANGE, -1) == 0) {
            guess_bd_context_menu.getItem(0).setVisible(true);
            guess_bd_context_menu.getItem(1).setVisible(false);
            guess_bd_context_menu.getItem(3).setVisible(true);
            guess_bd_context_menu.getItem(2).setVisible(false);
//            guess_bd_context_menu.getItem(5).setVisible(true);
//            guess_bd_context_menu.getItem(4).setVisible(false);
            guess_bd_context_menu.getItem(7).setVisible(true);
            guess_bd_context_menu.getItem(6).setVisible(false);
            guess_bd_context_menu.getItem(9).setVisible(true);
            guess_bd_context_menu.getItem(8).setVisible(false);
            guess_bd_context_menu.getItem(11).setVisible(true);
            guess_bd_context_menu.getItem(10).setVisible(false);
        }
        if (settings.getInt(PREF_VIEW_RANGE, -1) == 1) {
            guess_bd_context_menu.getItem(1).setVisible(true);
            guess_bd_context_menu.getItem(0).setVisible(false);
            guess_bd_context_menu.getItem(2).setVisible(true);
            guess_bd_context_menu.getItem(3).setVisible(false);
//            guess_bd_context_menu.getItem(5).setVisible(true);
//            guess_bd_context_menu.getItem(4).setVisible(false);
            guess_bd_context_menu.getItem(7).setVisible(true);
            guess_bd_context_menu.getItem(6).setVisible(false);
            guess_bd_context_menu.getItem(9).setVisible(true);
            guess_bd_context_menu.getItem(8).setVisible(false);
            guess_bd_context_menu.getItem(11).setVisible(true);
            guess_bd_context_menu.getItem(10).setVisible(false);
        }
        if (settings.getInt(PREF_VIEW_RANGE, -1) == 2) {
            guess_bd_context_menu.getItem(1).setVisible(true);
            guess_bd_context_menu.getItem(0).setVisible(false);
            guess_bd_context_menu.getItem(3).setVisible(true);
            guess_bd_context_menu.getItem(2).setVisible(false);
//            guess_bd_context_menu.getItem(4).setVisible(true);
//            guess_bd_context_menu.getItem(5).setVisible(false);
            guess_bd_context_menu.getItem(7).setVisible(true);
            guess_bd_context_menu.getItem(6).setVisible(false);
            guess_bd_context_menu.getItem(9).setVisible(true);
            guess_bd_context_menu.getItem(8).setVisible(false);
            guess_bd_context_menu.getItem(11).setVisible(true);
            guess_bd_context_menu.getItem(10).setVisible(false);
        }
        if (settings.getInt(PREF_VIEW_RANGE, -1) == 7) {
            guess_bd_context_menu.getItem(1).setVisible(true);
            guess_bd_context_menu.getItem(0).setVisible(false);
            guess_bd_context_menu.getItem(3).setVisible(true);
            guess_bd_context_menu.getItem(2).setVisible(false);
//            guess_bd_context_menu.getItem(5).setVisible(true);
//            guess_bd_context_menu.getItem(4).setVisible(false);
            guess_bd_context_menu.getItem(6).setVisible(true);
            guess_bd_context_menu.getItem(7).setVisible(false);
            guess_bd_context_menu.getItem(9).setVisible(true);
            guess_bd_context_menu.getItem(8).setVisible(false);
            guess_bd_context_menu.getItem(11).setVisible(true);
            guess_bd_context_menu.getItem(10).setVisible(false);
        }
        if (settings.getInt(PREF_VIEW_RANGE, -1) == 30) {
            guess_bd_context_menu.getItem(1).setVisible(true);
            guess_bd_context_menu.getItem(0).setVisible(false);
            guess_bd_context_menu.getItem(3).setVisible(true);
            guess_bd_context_menu.getItem(2).setVisible(false);
//            guess_bd_context_menu.getItem(5).setVisible(true);
//            guess_bd_context_menu.getItem(4).setVisible(false);
            guess_bd_context_menu.getItem(7).setVisible(true);
            guess_bd_context_menu.getItem(6).setVisible(false);
            guess_bd_context_menu.getItem(8).setVisible(true);
            guess_bd_context_menu.getItem(9).setVisible(false);
            guess_bd_context_menu.getItem(11).setVisible(true);
            guess_bd_context_menu.getItem(10).setVisible(false);
        }
        if (settings.getInt(PREF_VIEW_RANGE, -1) == -1) {
            guess_bd_context_menu.getItem(1).setVisible(true);
            guess_bd_context_menu.getItem(0).setVisible(false);
            guess_bd_context_menu.getItem(3).setVisible(true);
            guess_bd_context_menu.getItem(2).setVisible(false);
//            guess_bd_context_menu.getItem(5).setVisible(true);
//            guess_bd_context_menu.getItem(4).setVisible(false);
            guess_bd_context_menu.getItem(7).setVisible(true);
            guess_bd_context_menu.getItem(6).setVisible(false);
            guess_bd_context_menu.getItem(9).setVisible(true);
            guess_bd_context_menu.getItem(8).setVisible(false);
            guess_bd_context_menu.getItem(10).setVisible(true);
            guess_bd_context_menu.getItem(11).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        SharedPreferences.Editor prefEditor = settings.edit();

        switch (item.getItemId()) {
            case R.id.menu_current_on:
                prefEditor.putInt(PREF_VIEW_RANGE, 0);
                prefEditor.apply();
                guess_bd_context_menu.getItem(0).setVisible(true);
                guess_bd_context_menu.getItem(1).setVisible(false);
                guess_bd_context_menu.getItem(3).setVisible(true);
                guess_bd_context_menu.getItem(2).setVisible(false);
//                guess_bd_context_menu.getItem(5).setVisible(true);
//                guess_bd_context_menu.getItem(4).setVisible(false);
                guess_bd_context_menu.getItem(7).setVisible(true);
                guess_bd_context_menu.getItem(6).setVisible(false);
                guess_bd_context_menu.getItem(9).setVisible(true);
                guess_bd_context_menu.getItem(8).setVisible(false);
                guess_bd_context_menu.getItem(11).setVisible(true);
                guess_bd_context_menu.getItem(10).setVisible(false);

                db.close();
                userCursor.close();
                onResume();

                return true;

            case R.id.menu_day_on:
                prefEditor.putInt(PREF_VIEW_RANGE, 1);
                prefEditor.apply();
                guess_bd_context_menu.getItem(1).setVisible(true);
                guess_bd_context_menu.getItem(0).setVisible(false);
                guess_bd_context_menu.getItem(2).setVisible(true);
                guess_bd_context_menu.getItem(3).setVisible(false);
//                guess_bd_context_menu.getItem(5).setVisible(true);
//                guess_bd_context_menu.getItem(4).setVisible(false);
                guess_bd_context_menu.getItem(7).setVisible(true);
                guess_bd_context_menu.getItem(6).setVisible(false);
                guess_bd_context_menu.getItem(9).setVisible(true);
                guess_bd_context_menu.getItem(8).setVisible(false);
                guess_bd_context_menu.getItem(11).setVisible(true);
                guess_bd_context_menu.getItem(10).setVisible(false);

                db.close();
                userCursor.close();
                onResume();

                return true;

            case R.id.menu_yesterday_on:
                prefEditor.putInt(PREF_VIEW_RANGE, 2);
                prefEditor.apply();
                guess_bd_context_menu.getItem(1).setVisible(true);
                guess_bd_context_menu.getItem(0).setVisible(false);
                guess_bd_context_menu.getItem(3).setVisible(true);
                guess_bd_context_menu.getItem(2).setVisible(false);
//                guess_bd_context_menu.getItem(4).setVisible(true);
//                guess_bd_context_menu.getItem(5).setVisible(false);
                guess_bd_context_menu.getItem(7).setVisible(true);
                guess_bd_context_menu.getItem(6).setVisible(false);
                guess_bd_context_menu.getItem(9).setVisible(true);
                guess_bd_context_menu.getItem(8).setVisible(false);
                guess_bd_context_menu.getItem(11).setVisible(true);
                guess_bd_context_menu.getItem(10).setVisible(false);

                db.close();
                userCursor.close();
                onResume();

                return true;

            case R.id.menu_week_on:
                prefEditor.putInt(PREF_VIEW_RANGE, 7);
                prefEditor.apply();
                guess_bd_context_menu.getItem(1).setVisible(true);
                guess_bd_context_menu.getItem(0).setVisible(false);
                guess_bd_context_menu.getItem(3).setVisible(true);
                guess_bd_context_menu.getItem(2).setVisible(false);
//                guess_bd_context_menu.getItem(5).setVisible(true);
//                guess_bd_context_menu.getItem(4).setVisible(false);
                guess_bd_context_menu.getItem(6).setVisible(true);
                guess_bd_context_menu.getItem(7).setVisible(false);
                guess_bd_context_menu.getItem(9).setVisible(true);
                guess_bd_context_menu.getItem(8).setVisible(false);
                guess_bd_context_menu.getItem(11).setVisible(true);
                guess_bd_context_menu.getItem(10).setVisible(false);

                db.close();
                userCursor.close();
                onResume();

                return true;

            case R.id.menu_month_on:
                prefEditor.putInt(PREF_VIEW_RANGE, 30);
                prefEditor.apply();
                guess_bd_context_menu.getItem(1).setVisible(true);
                guess_bd_context_menu.getItem(0).setVisible(false);
                guess_bd_context_menu.getItem(3).setVisible(true);
                guess_bd_context_menu.getItem(2).setVisible(false);
//                guess_bd_context_menu.getItem(5).setVisible(true);
//                guess_bd_context_menu.getItem(4).setVisible(false);
                guess_bd_context_menu.getItem(7).setVisible(true);
                guess_bd_context_menu.getItem(6).setVisible(false);
                guess_bd_context_menu.getItem(8).setVisible(true);
                guess_bd_context_menu.getItem(9).setVisible(false);
                guess_bd_context_menu.getItem(11).setVisible(true);
                guess_bd_context_menu.getItem(10).setVisible(false);

                db.close();
                userCursor.close();
                onResume();

                return true;

            case R.id.menu_all_on:
                prefEditor.putInt(PREF_VIEW_RANGE, -1);
                prefEditor.apply();
                guess_bd_context_menu.getItem(1).setVisible(true);
                guess_bd_context_menu.getItem(0).setVisible(false);
                guess_bd_context_menu.getItem(3).setVisible(true);
                guess_bd_context_menu.getItem(2).setVisible(false);
//                guess_bd_context_menu.getItem(5).setVisible(true);
//                guess_bd_context_menu.getItem(4).setVisible(false);
                guess_bd_context_menu.getItem(7).setVisible(true);
                guess_bd_context_menu.getItem(6).setVisible(false);
                guess_bd_context_menu.getItem(9).setVisible(true);
                guess_bd_context_menu.getItem(8).setVisible(false);
                guess_bd_context_menu.getItem(10).setVisible(true);
                guess_bd_context_menu.getItem(11).setVisible(false);

                db.close();
                userCursor.close();
                onResume();

                return true;
        }
        return false;
    }


    public void idSort(View v) {  // 0
        SharedPreferences.Editor prefEditor = settings.edit();
        if (settings.getInt(PREF_SORT, 0) == 0) {
            if (settings.getInt(PREF_SORT_DIR, 0) == 0) {
                prefEditor.putInt(PREF_SORT_DIR, 1);
            }
            else {
                prefEditor.putInt(PREF_SORT_DIR, 0);
            }
        }
        else {
            prefEditor.putInt(PREF_SORT, 0);
//            DBidSort.setBackgroundColor(ContextCompat.getColor(this, R.color.board_color));
        }
        prefEditor.apply();

        db.close();
        userCursor.close();
        onResume();
    }
    public void dateSort(View v) {  // 1
        SharedPreferences.Editor prefEditor = settings.edit();
        if (settings.getInt(PREF_SORT, 0) == 1) {
            if (settings.getInt(PREF_SORT_DIR, 0) == 0) {
                prefEditor.putInt(PREF_SORT_DIR, 1);
            }
            else {
                prefEditor.putInt(PREF_SORT_DIR, 0);
            }
        }
        else {
            prefEditor.putInt(PREF_SORT, 1);
        }
        prefEditor.apply();

        db.close();
        userCursor.close();
        onResume();
    }
    public void movesSort(View v) {  // 2
        SharedPreferences.Editor prefEditor = settings.edit();
        if (settings.getInt(PREF_SORT, 0) == 2) {
            if (settings.getInt(PREF_SORT_DIR, 0) == 0) {
                prefEditor.putInt(PREF_SORT_DIR, 1);
            }
            else {
                prefEditor.putInt(PREF_SORT_DIR, 0);
            }
        }
        else {
            prefEditor.putInt(PREF_SORT, 2);
        }
        prefEditor.apply();

        db.close();
        userCursor.close();
        onResume();
    }
    public void seqSort(View v) {  // 3
        SharedPreferences.Editor prefEditor = settings.edit();
        if (settings.getInt(PREF_SORT, 0) == 3) {
            if (settings.getInt(PREF_SORT_DIR, 0) == 0) {
                prefEditor.putInt(PREF_SORT_DIR, 1);
            }
            else {
                prefEditor.putInt(PREF_SORT_DIR, 0);
            }
        }
        else {
            prefEditor.putInt(PREF_SORT, 3);
        }
        prefEditor.apply();

        db.close();
        userCursor.close();
        onResume();
    }
    public void goodSort(View v) {  // 4
        SharedPreferences.Editor prefEditor = settings.edit();
        if (settings.getInt(PREF_SORT, 0) == 4) {
            if (settings.getInt(PREF_SORT_DIR, 0) == 0) {
                prefEditor.putInt(PREF_SORT_DIR, 1);
            }
            else {
                prefEditor.putInt(PREF_SORT_DIR, 0);
            }
        }
        else {
            prefEditor.putInt(PREF_SORT, 4);
        }
        prefEditor.apply();

        db.close();
        userCursor.close();
        onResume();
    }
    public void badSort(View v) {  // 5
        SharedPreferences.Editor prefEditor = settings.edit();
        if (settings.getInt(PREF_SORT, 0) == 5) {
            if (settings.getInt(PREF_SORT_DIR, 0) == 0) {
                prefEditor.putInt(PREF_SORT_DIR, 1);
            }
            else {
                prefEditor.putInt(PREF_SORT_DIR, 0);
            }
        }
        else {
            prefEditor.putInt(PREF_SORT, 5);
        }
        prefEditor.apply();

        db.close();
        userCursor.close();
        onResume();
    }
    public void hintSort(View v) {  // 6
        SharedPreferences.Editor prefEditor = settings.edit();
        if (settings.getInt(PREF_SORT, 0) == 6) {
            if (settings.getInt(PREF_SORT_DIR, 0) == 0) {
                prefEditor.putInt(PREF_SORT_DIR, 1);
            }
            else {
                prefEditor.putInt(PREF_SORT_DIR, 0);
            }
        }
        else {
            prefEditor.putInt(PREF_SORT, 6);
        }
        prefEditor.apply();

        db.close();
        userCursor.close();
        onResume();
    }


}
