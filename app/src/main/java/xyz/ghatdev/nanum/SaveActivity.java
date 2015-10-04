package xyz.ghatdev.nanum;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class SaveActivity extends AppCompatActivity {

    private String name;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        Button btn_Save = (Button) findViewById(R.id.btn_save);
        final EditText fname = (EditText) findViewById(R.id.edit_filename);


        btn_Save.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                addTextToFile(text, fname.getText().toString() + ".txt");
            }
        });

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            switch (type) {
                case "text/plain": // 텍스트는 sd카드에 텍스트 파일로 저장
                    text = intent.getStringExtra(Intent.EXTRA_TEXT);
                    break;
                case "image/*":


            }
        }


    }


    public void addTextToFile(String text, String filename) {
        File logFile = new File(Environment.getExternalStorageDirectory() + File.separator + filename);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String saveToInternalSorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, "profile.jpg");

        FileOutputStream fos = null;
        try {

            fos = new FileOutputStream(mypath);

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }

}
