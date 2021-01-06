package de.earthlingz.oerszebra.guessmove;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static de.earthlingz.oerszebra.guessmove.GuessMoveModeManager.GlobalVars.db;

public class Utils extends AppCompatActivity {
    private final static String FILE_NAME = "Reversinstructor.txt";
    private static final int REQUEST_PERMISSION_WRITE = 1001;
    private boolean permissionGranted;

    Cursor userCursor;

    public Utils() {}

// =================================================================================================
// сохранение в базу данных
// =================================================================================================
    public void saveBDguess(Context context, long id, int move, String moveseq, boolean good, boolean bad, boolean hint, int eval){
        ContentValues cv = new ContentValues();

        long Id = -1L;
        int Move = 0;
        long Good = 0;
        long Bad = 0;
        long Hint = 0;
        int Eval = 0;
        int EvalMin = 0;

        userCursor = db.rawQuery("select * from "+ DBHelper.TABLE_GUESS + " ORDER BY " + "\"_id\"" + " ASC", null);
        userCursor.moveToFirst();

        Date date = new Date();     // Текущее время
//        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());  // Форматирование времени как "день.месяц.год"
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());  // Форматирование времени как "год.месяц.день"
        String dateText = dateFormat.format(date);

        Log.d("SYM777_DEBUG -->", "Now = " + dateText);

        while (!userCursor.isAfterLast())
        {
            String DateText = userCursor.getString(userCursor.getColumnIndex(DBHelper.COLUMN_STR_DATE));
            String MoveSeq = userCursor.getString(userCursor.getColumnIndex(DBHelper.COLUMN_STR_MOVESEQ));

            if (dateText.equals(DateText) && moveseq.equals(MoveSeq)) {
                try {
                    Id = Long.valueOf(userCursor.getString(userCursor.getColumnIndex(DBHelper.COLUMN_ID)));
                    Move = Integer.valueOf(userCursor.getString(userCursor.getColumnIndex(DBHelper.COLUMN_INT_MOVE)));
                    Good = Long.valueOf(userCursor.getString(userCursor.getColumnIndex(DBHelper.COLUMN_INT_GOOD)));
                    Bad = Long.valueOf(userCursor.getString(userCursor.getColumnIndex(DBHelper.COLUMN_INT_BAD)));
                    Hint = Long.valueOf(userCursor.getString(userCursor.getColumnIndex(DBHelper.COLUMN_INT_HINT)));
                    Eval = Integer.valueOf(userCursor.getString(userCursor.getColumnIndex(DBHelper.COLUMN_INT_EVAL)));
                    EvalMin = Integer.valueOf(userCursor.getString(userCursor.getColumnIndex(DBHelper.COLUMN_INT_EVALMIN)));
                } catch (NumberFormatException e) {
                    System.err.println("Неверный формат строки!");
                }
                break;
            }

            userCursor.moveToNext();
        }

        if (good) Good++;
        if (bad) {
            Bad++;
            Eval = (int)((float)(Eval*(Bad - 1) + eval) / (float)(Bad));
            if (eval < EvalMin) EvalMin = eval;

        }
        if (hint) Hint++;

//        dateText = "2020-01-21 15:55:55";
        cv.put(DBHelper.COLUMN_STR_DATE, dateText);

        if (Integer.toString(move).trim().length() != 0)    cv.put(DBHelper.COLUMN_INT_MOVE, move);
        if (moveseq.trim().length() != 0)                   cv.put(DBHelper.COLUMN_STR_MOVESEQ, moveseq);
        if (Long.toString(Good).trim().length() != 0)       cv.put(DBHelper.COLUMN_INT_GOOD, Good);
        if (Long.toString(Bad).trim().length() != 0)        cv.put(DBHelper.COLUMN_INT_BAD, Bad);
        if (Long.toString(Hint).trim().length() != 0)       cv.put(DBHelper.COLUMN_INT_HINT, Hint);
        if (Integer.toString(Eval).trim().length() != 0)    cv.put(DBHelper.COLUMN_INT_EVAL, Eval);
        if (Integer.toString(EvalMin).trim().length() != 0) cv.put(DBHelper.COLUMN_INT_EVALMIN, EvalMin);

        if (Id >= 0) {
            db.update(DBHelper.TABLE_GUESS, cv, DBHelper.COLUMN_ID + "=" + String.valueOf(Id), null);
        } else {
            db.insert(DBHelper.TABLE_GUESS, null, cv);
        }

        userCursor.close();
    }



    public long saveBDgame(Context context, String moveseq, int BlackDiscs, int WhiteDiscs){
        SQLiteDatabase dbGame;
        DBHelper sqlHelper;
        sqlHelper = new DBHelper(context);
        dbGame = sqlHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();

//        userCursor = dbGame.rawQuery("select * from "+ DBHelper.TABLE_GAME_SAVES + " ORDER BY " + "\"_id\"" + " ASC", null);

        Date date = new Date();     // Текущее время
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());  // Форматирование времени как "год.месяц.день"
        String dateText = dateFormat.format(date);

        Log.d("SYM777_DEBUG -->", "Now = " + dateText);

        cv.put(DBHelper.COLUMN_STR_DATE, dateText);

        if (moveseq.trim().length() != 0)                       cv.put(DBHelper.COLUMN_STR_MOVESEQ, moveseq);
        if (Long.toString(BlackDiscs).trim().length() != 0)     cv.put(DBHelper.COLUMN_INT_BDISCS, BlackDiscs);
        if (Long.toString(WhiteDiscs).trim().length() != 0)     cv.put(DBHelper.COLUMN_INT_WDISCS, WhiteDiscs);

        Long id = dbGame.insert(DBHelper.TABLE_GAME_SAVES, null, cv);

//        userCursor.close();
        dbGame.close();

        return id;
    }



    // =================================================================================================
// сохранение в текстовый файл
// =================================================================================================
    public void saveText(Context ctx, String text){
        if(!permissionGranted){
            if (checkPermissions(ctx)) Log.d("SYM777 DEBUG", "checkPermissions() = true");
            else {
                Log.d("SYM777 DEBUG", "checkPermissions() = false");
                return;
            }
        }
        FileOutputStream fos = null;
        try {
            String string = text + "\n";

            fos = new FileOutputStream(getExternalPath(), true);
            fos.write(string.getBytes());
//            Toast.makeText(ctx, "Запись сохранена в резервный файл TXT", Toast.LENGTH_SHORT).show();
        }
        catch(IOException ex) {
            Toast.makeText(ctx, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finally{
            try{
                if(fos!=null)
                    fos.close();
            }
            catch(IOException ex){
                Toast.makeText(ctx, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void newTextFile(Context ctx) {
        if(!permissionGranted){
            if (checkPermissions(ctx)) Log.d("SYM777 DEBUG", "checkPermissions() = true");
            else {
                Log.d("SYM777 DEBUG", "checkPermissions() = false");
                return;
            }
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(getExternalPath());
        }
        catch(IOException ex) {
            Toast.makeText(ctx, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finally{
            try{
                if(fos!=null)
                    fos.close();
            }
            catch(IOException ex){
                Toast.makeText(ctx, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File getExternalPath() {
        return(new File(Environment.getExternalStorageDirectory(), FILE_NAME));
    }

    // проверяем, доступно ли внешнее хранилище для чтения и записи
    private boolean isExternalStorageWriteable(){
        String state = Environment.getExternalStorageState();
        return  Environment.MEDIA_MOUNTED.equals(state);
    }
    // проверяем, доступно ли внешнее хранилище хотя бы только для чтения
    private boolean isExternalStorageReadable(){
        String state = Environment.getExternalStorageState();
        return  (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }

    private boolean checkPermissions(Context ctx){
        if(!isExternalStorageReadable() || !isExternalStorageWriteable()){
            Toast.makeText(ctx, "Внешнее хранилище не доступно", Toast.LENGTH_LONG).show();
            return false;
        }
        else Log.d("SYM777 DEBUG", "Внешнее хранилище доступно");
        int permissionCheck = ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE);
            return false;
        }
        else {
            permissionGranted = true;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if (requestCode == REQUEST_PERMISSION_WRITE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                permissionGranted = true;
                Toast.makeText(this, "Разрешения получены", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(this, "Необходимо дать разрешения", Toast.LENGTH_LONG).show();
            }
        }
    }


// =================================================================================================
// преобразование даты из строки в мс
// =================================================================================================
    public long getMSFromDate(String dateFormat) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy");
        try {
            date = formatter.parse(dateFormat);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

}
