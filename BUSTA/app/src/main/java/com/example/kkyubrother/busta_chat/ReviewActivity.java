package com.example.kkyubrother.busta_chat;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import static com.example.kkyubrother.busta_chat.R.id.btn_review;
import static com.example.kkyubrother.busta_chat.R.layout.activity_review;

/**
 * Created by genie on 2016-10-28.
 */

public class ReviewActivity extends AppCompatActivity {

    private Cursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_review);

        //Button btn_review = (Button)findViewById(btn_review);

        SQLiteDatabase db = openOrCreateDatabase("reviewlist.db", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS review (" +
                "_id		INTEGER PRIMARY KEY AUTOINCREMENT," +
                "date		TEXT," +
                "name		TEXT," +
                "title		TEXT," +
                "content		TEXT" +
                ");");

        db.close();
    }

    public void onClick(View view) {

        switch(view.getId()){
            case btn_review:
                Intent it = new Intent(this, ReviewMakeActivity.class);
                startActivity(it);
                break;
        }
    }

    protected void onResume() {
        super.onResume();
        //Log.i(TAG, "onResume");

        //ListViewFruit
        SQLiteDatabase db = openOrCreateDatabase("reviewlist.db", Context.MODE_PRIVATE, null); //DB Open
        ListView listView = (ListView)findViewById(R.id.lv_review); //ListView 열기
        mCursor = db.rawQuery("SELECT * FROM review", null); //쿼리 날리고
        mCursor.moveToFirst(); //커서 처음으로 보내고
        String[] from = new String[]{"date", "name", "title"}; //가져올 DB의 필드 이름
        int[] to = new int[]{R.id.review_date,R.id.name,R.id.tv_title}; //각각 대응되는 xml의 TextView의 id
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                listView.getContext(), //ListView의 context
                R.layout.review_item, //ListView의 Custom layout
                mCursor, //Item으로 사용할 DB의 Cursor
                from, //DB 필드 이름
                to //DB필드에 대응되는 xml TextView의 id
        );
        listView.setAdapter(adapter); //어댑터 등록
        db.close(); //DB를 닫음.
        //Cursor는 닫으면 안된다. Cursor 닫으면 리스트에 항목들 안뜬다. Cursor는

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position,
                                    long id) {

                Cursor c = (Cursor)adapter.getItem(position);
                String name = c.getString(2);
                String title = c.getString(3);
                String content = c.getString(4);
                //Toast.makeText(getApplicationContext(), note, Toast.LENGTH_LONG).show();

                Bundle extras = new Bundle();
                extras.putString("name", name);
                extras.putString("title", title);
                extras.putString("content", content);

                Intent intent = new Intent(ReviewActivity.this, ReviewViewActivity.class);
                intent.putExtras(extras);
                startActivity(intent);


            }
        });


    }



    @Override
    protected void onPause() {
        super.onPause();
        //Log.i(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Log.i(TAG, "onStop");

        if (mCursor != null)
            mCursor.close();
    }
}
