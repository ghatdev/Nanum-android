package xyz.ghatdev.nanum;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class ShareActivity extends AppCompatActivity {

    private String file;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        Intent intent = getIntent();
        String action = intent.getAction();
        type = intent.getType();
        Uri uri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
        file = uri.toString();


        if (file.startsWith("file:///")) {
            file = file.substring(7);
        } else if (file.startsWith("content://")) {
            Cursor cursor = getContentResolver().query(Uri.parse(file), null, null, null, null);
            cursor.moveToNext();
            file = cursor.getString(cursor.getColumnIndex("_data"));
            Log.d("test", file);
            cursor.close();
        }
        file = Uri.decode(file);


        Button btn_share = (Button) findViewById(R.id.btn_share);
        btn_share.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Upload();
                Intent act = new Intent(ShareActivity.this, UploadActivity.class);
                startActivity(act);
                finish();
            }

        });

    }

    private void Upload() {
        Intent service = new Intent(this, UploadService.class);
        service.putExtra("fname", file);
        service.putExtra("type", type);
        startService(service);
    }


}
