package xyz.ghatdev.nanum;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.exceptions.RealmMigrationNeededException;

public class UploadActivity extends AppCompatActivity {

    private ListView mListView = null;
    private ListviewAdapter mAdapter = null;

    private static final int PICKFILE_RESULT_CODE = 1;

    private UploadsDialog dialog;
    private Realm db;

    private EventBus bus = EventBus.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        bus.register(this);

        try{
            db = Realm.getDefaultInstance();
        }
        catch (RealmMigrationNeededException e)
        {

        }

        RealmQuery<FileData> query = db.where(FileData.class);

        mListView = (ListView)findViewById(R.id.listView);
        mAdapter = new ListviewAdapter(this);
        mListView.setAdapter(mAdapter);

        RealmResults<FileData> results = query.findAll();

        for(FileData f : results)
        {
            int i=0;
            String type = f.getType();
            switch (type.substring(0,type.lastIndexOf("/")))
            {
                case "application":
                    i=R.drawable.google134;
                    break;
                case "image":
                    i=R.drawable.image;
                    break;
                case "video":
                    i=R.drawable.film61;
                    break;
                case "audio":
                    i=R.drawable.headset11;
                    break;
                case "multipart":
                    i=R.drawable.folder215;
                    break;
            }
            mAdapter.addItem(i, f.getFileName(),f.getKey());
        }
        db.close();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast toast;
                ListData listData = mAdapter.mListData.get(position);
                db = Realm.getDefaultInstance();
                RealmResults<FileData> realmResults = db.where(FileData.class).findAll();
                FileData f = realmResults.where().equalTo("key", listData.key).findFirst();
                if (f.getUuid().equals("")) {
                    toast = Toast.makeText(UploadActivity.this, R.string.toast_filenot, Toast.LENGTH_SHORT);
                    toast.show();
                } else if (f.getKey() == listData.key) {
                    String uuid = f.getUuid();
                    dialog = new UploadsDialog();
                    Bundle bundle = new Bundle();
                    bundle.putString("fname", f.getFileName());
                    bundle.putString("uuid", uuid);
                    bundle.putString("path", f.getPath());
                    bundle.putString("type", f.getType());
                    dialog.setArguments(bundle);
                    dialog.show(getFragmentManager(), "NANUM");
                }
                db.close();
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            private ListData listData;
            private int p;

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                p=position;
                AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
                listData = mAdapter.mListData.get(position);
                builder.setTitle(listData.filenmae);
                builder.setMessage(R.string.askdelete);
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(R.string.text_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db = Realm.getDefaultInstance();
                        db.beginTransaction();
                        Toast toast;
                        RealmResults<FileData> realmResults = db.where(FileData.class).findAll();
                        FileData f = realmResults.where().equalTo("key", listData.key).findFirst();
                        if (f.getUuid().equals("")) {
                            toast = Toast.makeText(UploadActivity.this, R.string.toast_filenot, Toast.LENGTH_SHORT);
                            toast.show();
                        } else if (f.getKey() == listData.key) {
                            realmResults.remove(0);
                            toast=Toast.makeText(UploadActivity.this,R.string.deleted,Toast.LENGTH_SHORT);
                            toast.show();
                            db.commitTransaction();
                            mAdapter.mListData.remove(p);
                            mAdapter.dataChange();
                        }
                        db.close();
                    }
                });
                builder.create().show();
                return true;
            }
        });

        FloatingActionButton button = (FloatingActionButton)findViewById(R.id.fab);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

    }



    private void refresh()
    {
        try{
            RealmQuery<FileData> query = db.where(FileData.class);

            mListView = (ListView)findViewById(R.id.listView);
            mAdapter = new ListviewAdapter(this);
            mListView.setAdapter(mAdapter);

            RealmResults<FileData> results = query.findAll();

            for(FileData f : results)
            {
                int i=0;
                String type = f.getType();
                switch (type.substring(0,type.lastIndexOf("/")))
                {
                    case "application":
                        i=R.drawable.google134;
                        break;
                    case "image":
                        i=R.drawable.image;
                        break;
                    case "video":
                        i=R.drawable.film61;
                        break;
                    case "audio":
                        i=R.drawable.headset11;
                        break;
                    case "multipart":
                        i=R.drawable.folder215;
                        break;
                }
                mAdapter.addItem(i, f.getFileName(),f.getKey());
            }
            db.close();
        }
        catch (Exception e)
        {
            Toast toast = Toast.makeText(this,R.string.toomanyrefresh,Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case PICKFILE_RESULT_CODE:
                if(resultCode==RESULT_OK){
                    String file;
                    String type;
                    file = data.getData().getPath();
                    Uri uri=Uri.parse(data.getData().getPath());
                    Log.d("file",file);

                    file = Uri.decode(file);
                    type= getContentResolver().getType(Uri.parse(file));
                    Log.d("test",type);
                    Intent service = new Intent(this,UploadService.class);
                    service.putExtra("fname", file);
                    service.putExtra("type",type);
                    //startService(service);
                }
                break;

        }
    }

    private class ListviewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<ListData> mListData = new ArrayList<ListData>();

        public ListviewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null)
            {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.uploadlist_layout,null);

                holder.mText = (TextView) convertView.findViewById(R.id.filename_text);
                holder.imageView = (ImageView) convertView.findViewById(R.id.fileicon);

                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }

            ListData mData = mListData.get(position);



            holder.mText.setText(mData.filenmae);
            holder.imageView.setImageResource(mData.ico);

            return convertView;
        }

        public void addItem(int icon,String filename,int k){
            ListData addInfo = null;
            addInfo = new ListData();
            addInfo.ico=icon;
            addInfo.filenmae = filename;
            addInfo.key = k;
            mListData.add(addInfo);
        }


        public void remove(int position){
            mListData.remove(position);
            dataChange();
        }


        public void dataChange(){
            mAdapter.notifyDataSetChanged();
        }

    }

    public static class UploadsDialog extends DialogFragment
    {
        private Bundle bundle;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater layoutInflater = getActivity().getLayoutInflater();
            bundle = getArguments();
            View view = layoutInflater.inflate(R.layout.dialog_uploads,null);
            ImageView img = (ImageView)view.findViewById(R.id.imageView);
            if(bundle.getString("type").equals("image/*"))
            {
                Bitmap bitmap = BitmapFactory.decodeFile(bundle.getString("path"));
                img.setImageBitmap(bitmap);
            }
            else
            {
                int i=0;
                String type = bundle.getString("type");
                switch (type.substring(0,type.lastIndexOf("/")))
                {
                    case "application":
                        i=R.drawable.google134;
                        break;
                    case "video":
                        i=R.drawable.film61;
                        break;
                    case "audio":
                        i=R.drawable.headset11;
                        break;
                    case "multipart":
                        i=R.drawable.folder215;
                        break;
                }
                img.setImageResource(i);
            }
            TextView textView = (TextView)view.findViewById(R.id.text_url);
            textView.setText(bundle.getString("uuid"));
            builder.setView(view)
            .setNegativeButton(R.string.text_copylink, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast toast;
                    ClipData clipData;
                    ClipboardManager myClipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                    toast = Toast.makeText(getActivity(), R.string.file_uploaded, Toast.LENGTH_SHORT);
                    toast.show();
                    clipData = ClipData.newPlainText("Nanum Link", "http://api.ghatdev.xyz/File/Get/" + bundle.getString("uuid"));
                    myClipboard.setPrimaryClip(clipData);
                }
            })
            .setPositiveButton(R.string.btn_sharelink, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //링크공유 인텐트
                    Intent link = new Intent(Intent.ACTION_SEND);
                    link.putExtra(Intent.EXTRA_TEXT, "http://api.ghatdev.xyz/File/Get/" + bundle.getString("uuid"));
                    link.setType("text/plain");
                    startActivity(Intent.createChooser(link, getString(R.string.share)));
                }
            })
            .setNeutralButton(R.string.btn_closedialog, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.setTitle(bundle.getString("fname"));

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast toast;
                    ClipData clipData;
                    ClipboardManager myClipboard = (ClipboardManager)getActivity().getSystemService(CLIPBOARD_SERVICE);
                    toast = Toast.makeText(getActivity(),R.string.file_uploaded,Toast.LENGTH_SHORT);
                    toast.show();
                    clipData = ClipData.newPlainText("Nanum Link", "http://api.ghatdev.xyz/File/Get/" + bundle.getString("uuid"));
                    myClipboard.setPrimaryClip(clipData);
                }
            });

            return builder.create();
        }
    }

    @Override
    protected void onDestroy()
    {
        bus.unregister(this);
        super.onDestroy();
    }

    public void onEvent(BusEvent event)
    {
        refresh();
    }

}



