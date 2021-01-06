package de.earthlingz.oerszebra;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.widget.ImageButton;

import android.util.Log;
import android.widget.Toast;

import de.earthlingz.oerszebra.guessmove.DBHelper;
import de.earthlingz.oerszebra.guessmove.Utils;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class SaveGameActivity extends AppCompatActivity {

    TextView currentDate;
    TextView currentTime;
    EditText BPlayer;
    EditText WPlayer;
    TextView BDiscs;
    TextView WDiscs;
    EditText textNotice;
    Button delButton;
    Button saveButton;
    LinearLayout inputLayout;

    Calendar dateAndTime=Calendar.getInstance();

    DBHelper sqlHelper;
    SQLiteDatabase db;
    Cursor userCursor;
    long userId=0;

    private Menu menu; // Global Menu Declaration


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_game);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        currentDate = (TextView) findViewById(R.id.current_date);
        currentTime = (TextView) findViewById(R.id.current_time);
        BPlayer = (EditText) findViewById(R.id.BPlayer);
        WPlayer = (EditText) findViewById(R.id.WPlayer);
        BDiscs = (TextView) findViewById(R.id.BDiscs);
        WDiscs = (TextView) findViewById(R.id.WDiscs);
        textNotice = (EditText) findViewById(R.id.notice);

        delButton = (Button) findViewById(R.id.deleteButtonText);
        saveButton = (Button) findViewById(R.id.saveButtonText);

        inputLayout = (LinearLayout) findViewById(R.id.input_layout);

// Текущее время
        Date date = new Date();
// Форматирование времени как "день.месяц.год"
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());
        String dateText = dateFormat.format(date);
        currentDate.setText(dateText);

        sqlHelper = new DBHelper(this);
        db = sqlHelper.getWritableDatabase();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getLong("id");
        }
// если 0, то добавление
        if (userId > 0) {
// получаем элемент по id из БД
            StringBuilder sqlQuery_builder = new StringBuilder();

            sqlQuery_builder.append("SELECT ").append(DBHelper.COLUMN_ID).append(", ")
                    .append("strftime('%d.%m.%Y', MAX(").append(DBHelper.COLUMN_STR_DATE).append("))").append(" date_text").append(", ")
                    .append("strftime('%H:%M:%S', MAX(").append(DBHelper.COLUMN_STR_DATE).append("))").append(" time_text").append(", ")
                    .append(DBHelper.COLUMN_STR_MOVESEQ).append(", ")
                    .append(DBHelper.COLUMN_STR_BPLAYER).append(", ")
                    .append(DBHelper.COLUMN_STR_WPLAYER).append(", ")
                    .append(DBHelper.COLUMN_INT_BDISCS).append(", ")
                    .append(DBHelper.COLUMN_INT_WDISCS).append(", ")
                    .append(DBHelper.COLUMN_STR_NOTE)
                    .append(" FROM ").append(DBHelper.TABLE_GAME_SAVES)
                    .append(" WHERE ").append(DBHelper.COLUMN_ID).append(" = ").append(Long.toString(userId));

            String sqlQuery = sqlQuery_builder.toString().replace("null", "");

            Log.d("SYM777_DEBUG -->", sqlQuery);

            //получаем данные из бд в виде курсора
//        userCursor = db.rawQuery("select * from "+ DBHelper.TABLE_DAILY + " ORDER BY " + "\"_id\"" + " DESC", null);
            userCursor = db.rawQuery(sqlQuery, null);

            userCursor.moveToFirst();

            currentDate.setText(userCursor.getString(1));
            currentTime.setText(userCursor.getString(2));
            BPlayer.setText(userCursor.getString(4));
            WPlayer.setText(userCursor.getString(5));
            if (userCursor.getInt(6) > 0)
                BDiscs.setText(String.valueOf(userCursor.getInt(6)));
            if (userCursor.getInt(7) > 0)
                WDiscs.setText(String.valueOf(userCursor.getInt(7)));
            textNotice.setText(userCursor.getString(8));

            userCursor.close();
        } else {
// скрываем кнопку удаления
            delButton.setVisibility(View.GONE);
        }

        if (extras != null) {
            if (extras.getBoolean("new")) {
                delButton.setVisibility(View.GONE);
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();

    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }


    public void save(){
/*        ContentValues cv = new ContentValues();
        Utils mUtils = new Utils();
        cv.put(DBHelper.COLUMN_STR_DATE, currentDate.getText().toString());
        cv.put(DBHelper.COLUMN_STR_BPLAYER, currentDate.getText().toString());
        cv.put(DBHelper.COLUMN_STR_WPLAYER, currentDate.getText().toString());
        if (BDiscs.getText().toString().trim().length() != 0)
            cv.put(DBHelper.COLUMN_INT_BDISCS, Integer.parseInt(BDiscs.getText().toString()));
        if (WDiscs.getText().toString().trim().length() != 0)
            cv.put(DBHelper.COLUMN_INT_WDISCS, Integer.parseInt(WDiscs.getText().toString()));
        cv.put(DBHelper.COLUMN_STR_NOTE, textNotice.getText().toString());

        if (userId > 0) {
            db.update(DBHelper.TABLE_GAME_SAVES, cv, DBHelper.COLUMN_ID + "=" + String.valueOf(userId), null);
        } else {
            db.insert(DBHelper.TABLE_GAME_SAVES, null, cv);
        }

        goBack();*/
    }

    public void save(View v){
        ContentValues cv = new ContentValues();
        Utils mUtils = new Utils();

        Date date = new Date();
        try{
            date = new SimpleDateFormat("dd.MM.yy HH:mm:ss", Locale.ENGLISH).parse(currentDate.getText().toString() + " " + currentTime.getText().toString());
        }catch(Exception e){
            e.printStackTrace();
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());  // Форматирование времени как "год.месяц.день"
        String dateText = dateFormat.format(date);

        cv.put(DBHelper.COLUMN_STR_DATE, dateText);
        cv.put(DBHelper.COLUMN_STR_BPLAYER, BPlayer.getText().toString());
        cv.put(DBHelper.COLUMN_STR_WPLAYER, WPlayer.getText().toString());
/*        if (BDiscs.getText().toString().trim().length() != 0)
            cv.put(DBHelper.COLUMN_INT_BDISCS, Integer.parseInt(BDiscs.getText().toString()));
        if (WDiscs.getText().toString().trim().length() != 0)
            cv.put(DBHelper.COLUMN_INT_WDISCS, Integer.parseInt(WDiscs.getText().toString()));*/
        cv.put(DBHelper.COLUMN_STR_NOTE, textNotice.getText().toString());

        if (userId > 0) {
            db.update(DBHelper.TABLE_GAME_SAVES, cv, DBHelper.COLUMN_ID + "=" + String.valueOf(userId), null);
        } else {
            db.insert(DBHelper.TABLE_GAME_SAVES, null, cv);
        }

        Toast.makeText(this, "Игра сохранена", Toast.LENGTH_SHORT).show();

        goBack();
    }

    public void delete(){
        db.delete(DBHelper.TABLE_GAME_SAVES, "_id = ?", new String[]{String.valueOf(userId)});
        goBack();
    }

    public void delete(View v){
        AlertDialog.Builder ad;

        ad = new AlertDialog.Builder(this);
        ad.setTitle("Удаляем?");  // заголовок
//        ad.setMessage("Удаляем?"); // сообщение
        ad.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

                db.delete(DBHelper.TABLE_GAME_SAVES, "_id = ?", new String[]{String.valueOf(userId)});

                Toast.makeText(SaveGameActivity.this, "Удалено", Toast.LENGTH_SHORT).show();

                goBack();
            }
        });
        ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                Toast.makeText(SaveGameActivity.this, "Не удалено", Toast.LENGTH_SHORT).show();

//                goBack();
            }
        });
/*        ad.setCancelable(true);
        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(SaveGameActivity.this, "Отмена", Toast.LENGTH_SHORT).show();
            }
        });*/

        ad.show();

    }



    // отображаем диалоговое окно для выбора даты
    public void setDate(View v) {

        String[] arrayDateStr = currentDate.getText().toString().split("\\.");

        int intDay = Integer.parseInt(arrayDateStr[0]);
        int intMonth = Integer.parseInt(arrayDateStr[1]);
        int intYear = Integer.parseInt(arrayDateStr[2]) + 2000;

        new DatePickerDialog(SaveGameActivity.this, d, intYear, intMonth - 1, intDay).show();
    }

    // установка обработчика выбора даты
    DatePickerDialog.OnDateSetListener d=new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            setInitialDateTime();
        }
    };

    // обновление текстового поля с датой
    private void setInitialDateTime() {

        currentDate.setText(DateUtils.formatDateTime(this,
                dateAndTime.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR).replace('/', '.'));
    }



    private void goBack(){
        // закрываем подключение
        db.close();
        // переход к главной activity
        Intent intent = new Intent(this, DBgameSavedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.game_bd_context_menu, menu);

        if (userId == 0)
            menu.getItem(1).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

/*        if (id == R.id.action_save) {
            save();
            return true;
        }

        if (id == R.id.action_delete) {
            delete();
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }
}

