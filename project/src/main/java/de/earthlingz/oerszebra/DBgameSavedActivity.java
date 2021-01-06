package de.earthlingz.oerszebra;

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
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.content.ContextCompat;

import java.util.Locale;

import de.earthlingz.oerszebra.guessmove.DBHelper;
import de.earthlingz.oerszebra.guessmove.GuessMoveModeManager;

import static android.view.View.VISIBLE;

public class DBgameSavedActivity extends AppCompatActivity  {

    ListView userList;
    DBHelper databaseHelper;
    SQLiteDatabase db;
    Cursor userCursor;
    SimpleCursorAdapter userAdapter;

    private static final String PREFS_NAME = "BDsettings";
    private static final String PREF_VIEW_RANGE = "ViewRange";  // 0 / 1 / 2 / 7 / 30 / -1 /
    private static final String PREF_GAME_SORT = "PrefSort";     // 0  1
                                                                // id date
    private static final String PREF_GAME_SORT_DIR = "PrefSortDir";  // 0 / 1
    SharedPreferences settings;

    private Menu game_bd_context_menu;

    FrameLayout DBdateGameFrame;
    FrameLayout DBblackGameFrame;
    FrameLayout DBwhiteGameFrame;

    Button dateGameSortButton;
    Button blackGameSortButton;
    Button whiteGameSortButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_game);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        DBdateGameFrame = findViewById(R.id.DBdateGameFrame);
        DBblackGameFrame = findViewById(R.id.DBblackGameFrame);
        DBwhiteGameFrame = findViewById(R.id.DBwhiteGameFrame);

        dateGameSortButton = (Button)findViewById(R.id.dateGameSortButton);
        blackGameSortButton = (Button)findViewById(R.id.blackGameSortButton);
        whiteGameSortButton = (Button)findViewById(R.id.whiteGameSortButton);

        userList = (ListView)findViewById(R.id.listGame);

        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
// получаем элемент по id из БД
                userCursor = db.rawQuery("select * from " + DBHelper.TABLE_GAME_SAVES + " where " +
                        DBHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
                userCursor.moveToFirst();

                GuessMoveModeManager.GlobalVars.moveSequence1 = userCursor.getString(2);
                GuessMoveModeManager.GlobalVars.Restore = true;

                userCursor.close();

                goHome();
            }
        });

        userList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), SaveGameActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
                return true;
            }
        });


        View.OnClickListener oclBtn = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int btnIndex = 0;

                switch (v.getId()) {
                    case R.id.dateGameSortButton:
                        btnIndex = 1;
                        break;
                    case R.id.blackGameSortButton:
                        btnIndex = 3;
                        break;
                    case R.id.whiteGameSortButton:
                        btnIndex = 4;
                        break;
                }

                SharedPreferences.Editor prefEditor = settings.edit();
                if (settings.getInt(PREF_GAME_SORT, 1) == btnIndex) {
                    if (settings.getInt(PREF_GAME_SORT_DIR, 0) == 0) {
                        prefEditor.putInt(PREF_GAME_SORT_DIR, 1);
                    }
                    else {
                        prefEditor.putInt(PREF_GAME_SORT_DIR, 0);
                    }
                }
                else {
                    prefEditor.putInt(PREF_GAME_SORT, btnIndex);
                }
                prefEditor.apply();

                db.close();
                userCursor.close();
                onResume();
            }
        };

        dateGameSortButton.setOnClickListener(oclBtn);
        blackGameSortButton.setOnClickListener(oclBtn);
        whiteGameSortButton.setOnClickListener(oclBtn);


        databaseHelper = new DBHelper(this);
    }

    private void goHome(){
        // закрываем подключение
        db.close();
        // переход к главной activity
        Intent intent = new Intent(this, DroidZebra.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }


    @Override
    public void onResume() {
        super.onResume();

        String sortCondition = "_id ASC";

        DBdateGameFrame.setBackgroundColor(ContextCompat.getColor(this, R.color.board_color));
        DBblackGameFrame.setBackgroundColor(ContextCompat.getColor(this, R.color.board_color));
        DBwhiteGameFrame.setBackgroundColor(ContextCompat.getColor(this, R.color.board_color));

        switch (settings.getInt(PREF_GAME_SORT, 1)) {
/*            case 0:
                sortCondition = "_id ";
                if (settings.getInt(PREF_GAME_SORT_DIR, 0) == 0) {sortCondition += "ASC";} else {sortCondition += "DESC";}
                DBidGameFrame.setBackgroundColor(ContextCompat.getColor(this, R.color.Evals));
                break;*/
            case 1:
                sortCondition = "date_text ";
                if (settings.getInt(PREF_GAME_SORT_DIR, 0) == 0) {sortCondition += "ASC";} else {sortCondition += "DESC";}
//                sortCondition += ", time_text ";
//                if (settings.getInt(PREF_GAME_SORT_DIR, 0) == 0) {sortCondition += "ASC";} else {sortCondition += "DESC";}
                DBdateGameFrame.setBackgroundColor(ContextCompat.getColor(this, R.color.Evals));
                break;
            case 3:
                sortCondition = "black_player ";
                if (settings.getInt(PREF_GAME_SORT_DIR, 0) == 0) {sortCondition += "ASC";} else {sortCondition += "DESC";}
                sortCondition += ", black_discs "; sortCondition += "DESC";
                DBblackGameFrame.setBackgroundColor(ContextCompat.getColor(this, R.color.Evals));
                break;
            case 4:
                sortCondition = "white_player ";
                if (settings.getInt(PREF_GAME_SORT_DIR, 0) == 0) {sortCondition += "ASC";} else {sortCondition += "DESC";}
                sortCondition += ", white_discs "; sortCondition += "DESC";
                DBwhiteGameFrame.setBackgroundColor(ContextCompat.getColor(this, R.color.Evals));
                break;
            default:
                sortCondition = "date_text ";
                if (settings.getInt(PREF_GAME_SORT_DIR, 0) == 0) {sortCondition += "ASC";} else {sortCondition += "DESC";}
                sortCondition += ", time_text ";
                if (settings.getInt(PREF_GAME_SORT_DIR, 0) == 0) {sortCondition += "ASC";} else {sortCondition += "DESC";}
                DBdateGameFrame.setBackgroundColor(ContextCompat.getColor(this, R.color.Evals));
        }


        // открываем подключение
        db = databaseHelper.getReadableDatabase();

        StringBuilder sqlQuery_builder = new StringBuilder();

        sqlQuery_builder.append("SELECT ").append(DBHelper.COLUMN_ID).append(", ")
                .append("strftime('%d.%m.%Y', MAX(").append(DBHelper.COLUMN_STR_DATE).append("))").append(" date_text1").append(", ")
                .append("strftime('%H:%M:%S', MAX(").append(DBHelper.COLUMN_STR_DATE).append("))").append(" time_text").append(", ")
                .append(DBHelper.COLUMN_STR_MOVESEQ).append(", ")
                .append(DBHelper.COLUMN_STR_BPLAYER).append(", ")
                .append(DBHelper.COLUMN_STR_WPLAYER).append(", ")
                .append(DBHelper.COLUMN_INT_BDISCS).append(", ")
                .append(DBHelper.COLUMN_INT_WDISCS).append(", ")
                .append(DBHelper.COLUMN_STR_NOTE)
                .append(" FROM ").append(DBHelper.TABLE_GAME_SAVES)
                .append(" GROUP BY ").append(DBHelper.COLUMN_ID)
                .append(" ORDER BY ").append(sortCondition);

        String sqlQuery = sqlQuery_builder.toString().replace("null", "");

        Log.d("SYM777_DEBUG -->", sqlQuery);

        //получаем данные из бд в виде курсора
//        userCursor = db.rawQuery("select * from "+ DBHelper.TABLE_DAILY + " ORDER BY " + "\"_id\"" + " DESC", null);
        userCursor = db.rawQuery(sqlQuery, null);
        // определяем, какие столбцы из курсора будут выводиться в ListView
        String[] headers = new String[] {DBHelper.COLUMN_ID, "date_text1", "time_text", DBHelper.COLUMN_STR_MOVESEQ,
                DBHelper.COLUMN_STR_BPLAYER, DBHelper.COLUMN_STR_WPLAYER, DBHelper.COLUMN_INT_BDISCS, DBHelper.COLUMN_INT_WDISCS, DBHelper.COLUMN_STR_NOTE};
        // создаем адаптер, передаем в него курсор
        userAdapter = new SimpleCursorAdapter(this, R.layout.list_item_game,
                userCursor, headers, new int[]{R.id.text_id, R.id.text_date, R.id.text_time, R.id.text_moveseq,
                R.id.text_black_player, R.id.text_white_player, R.id.text_black_discs, R.id.text_white_discs, R.id.text_note}, 0);
        userList.setAdapter(userAdapter);

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

    @Override
    public void onBackPressed() {
        goHome();
    }

    /* Creates the menu items */
    @Override
    @SuppressLint("RestrictedApi")
    public boolean onCreateOptionsMenu(Menu menu) {
        this.game_bd_context_menu = menu;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_bd_context_menu, menu);

        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

/*    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        SharedPreferences.Editor prefEditor = settings.edit();

        switch (item.getItemId()) {

            case R.id.menu_month_on:
//                prefEditor.putInt(PREF_VIEW_RANGE, 30);
//                prefEditor.apply();

//                db.close();
//                userCursor.close();
//                onResume();

                return true;

        }
        return false;
    }*/

}

