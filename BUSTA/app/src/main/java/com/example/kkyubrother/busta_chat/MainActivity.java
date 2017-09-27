package com.example.kkyubrother.busta_chat;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.Toast;

import static com.example.kkyubrother.busta_chat.R.id.btn_chat;
import static com.example.kkyubrother.busta_chat.R.id.btn_drop;
import static com.example.kkyubrother.busta_chat.R.id.btn_rego;
import static com.example.kkyubrother.busta_chat.R.id.iv_rhkdwkd;
import static com.example.kkyubrother.busta_chat.R.id.request;

public class MainActivity extends AppCompatActivity {

    static TabHost host;
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    private Cursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ImageView iv_rhkdwkd = (ImageView)findViewById(iv_rhkdwkd);
        Button btn_request = (Button)findViewById(request);
       // Button btn_drop = (Button)findViewById(btn_drop);
        //Button btn_rego = (Button)findViewById(btn_rego);

        host = (TabHost) findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Tab One");
        spec.setContent(R.id.tab1);
        spec.setIndicator("", getResources().getDrawable(R.drawable.footer_icon1));
        host.addTab(spec);

        SQLiteDatabase db = openOrCreateDatabase("roter.db", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS list (" +
                "_id		INTEGER PRIMARY KEY AUTOINCREMENT," +
                "date		TEXT," +
                "time		TEXT," +
                "place		TEXT," +
                "team		TEXT" +
                ");");

        db.close();

        //Tab 2
        spec = host.newTabSpec("Tab Two");
        spec.setContent(R.id.tab2);
        spec.setIndicator("", getResources().getDrawable(R.drawable.menu_icon2));
        host.addTab(spec);

        //Tab 3
        spec = host.newTabSpec("Tab Three");
        spec.setContent(R.id.tab3);
        spec.setIndicator("", getResources().getDrawable(R.drawable.footer_icon3));
        host.addTab(spec);

        //Tab 4
        spec = host.newTabSpec("tab4");
        spec.setContent(R.id.tab4);
        spec.setIndicator("", getResources().getDrawable(R.drawable.footer_icon4));
        host.addTab(spec);

        host.setCurrentTab(2);
    }

    public void onClick(View view) {

        //탭2
        EditText et_team = (EditText)findViewById(R.id.TeamName);
        EditText et_date1 = (EditText)findViewById(R.id.Date2);
        EditText et_date2 = (EditText)findViewById(R.id.Date3);
        EditText et_time1 = (EditText)findViewById(R.id.Time1);
        EditText et_time2 = (EditText)findViewById(R.id.Time2);
        EditText et_place = (EditText)findViewById(R.id.Place);
        EditText et_email = (EditText)findViewById(R.id.Email);
        EditText et_pnum = (EditText)findViewById(R.id.PhoneNum);

        switch (view.getId()) {
            case iv_rhkdwkd:
                host.setCurrentTab(3);
                break;
            case request:
                String date = et_date1.getText().toString() +"/"+ et_date2.getText().toString();
                String time = et_time1.getText().toString() +":"+ et_time2.getText().toString();
                SQLiteDatabase db = openOrCreateDatabase("roter.db", Context.MODE_PRIVATE, null);
                ContentValues cv = new ContentValues();
	        	cv.put("date", date);
		        cv.put("time", time);
		        cv.put("place", et_place.getText().toString());
		        cv.put("team", et_team.getText().toString());
		        db.insert("list", null, cv);
		        db.close();
                onResume();

                Toast.makeText(this, "신청 완료 되었습니다.", Toast.LENGTH_LONG).show();

                et_team.setText("");
                et_date1.setText("");
                et_date2.setText("");
                et_time1.setText("");
                et_time2.setText("");
                et_place.setText("");
                et_email.setText("");
                et_pnum.setText("");
                break;
            case btn_drop:
                onDrop();
                onResume();
                break;
            case btn_rego:
                Intent it = new Intent(this, ReviewActivity.class);
                startActivity(it);
                break;
            case btn_chat:
                Intent it2 = new Intent(this, DPushActivity.class);
                startActivity(it2);
                break;

        }
    }

    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if(0<=intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "한 번 더 누르면 종료 됩니다.", Toast.LENGTH_LONG).show();
        }
    }

    protected void onDrop() {
        SQLiteDatabase db = openOrCreateDatabase("roter.db", Context.MODE_PRIVATE, null); //DB Open
        db.execSQL("DROP TABLE IF EXISTS list");
        db.execSQL("CREATE TABLE IF NOT EXISTS list (" +
                "_id		INTEGER PRIMARY KEY AUTOINCREMENT," +
                "date		TEXT," +
                "time		TEXT," +
                "place		TEXT," +
                "team		TEXT" +
                ");");

        db.close();
    }

    protected void onResume() {
        super.onResume();
        //Log.i(TAG, "onResume");

        //ListViewFruit
        SQLiteDatabase db = openOrCreateDatabase("roter.db", Context.MODE_PRIVATE, null); //DB Open
        ListView listView = (ListView)findViewById(R.id.lv_list); //ListView 열기
        ListView listView1 = (ListView)findViewById(R.id.lv_main_list); //메인탭 리스트뷰
        mCursor = db.rawQuery("SELECT * FROM list", null); //쿼리 날리고
        mCursor.moveToFirst(); //커서 처음으로 보내고
        String[] from = new String[]{"_id","date", "time", "place", "team"}; //가져올 DB의 필드 이름
        int[] to = new int[]{R.id.tv_no,R.id.tv_date,R.id.tv_time,R.id.tv_place,R.id.tv_team}; //각각 대응되는 xml의 TextView의 id
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                listView.getContext(), //ListView의 context
                R.layout.list_rhddus, //ListView의 Custom layout
                mCursor, //Item으로 사용할 DB의 Cursor
                from, //DB 필드 이름
                to //DB필드에 대응되는 xml TextView의 id
        );
        listView.setAdapter(adapter); //어댑터 등록
        listView1.setAdapter(adapter);
        db.close(); //DB를 닫음.
        //Cursor는 닫으면 안된다. Cursor 닫으면 리스트에 항목들 안뜬다. Cursor는

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position,
                                    long id) {

                Cursor c = (Cursor)adapter.getItem(position);
                String note = c.getString(3); //note는 3번임.(4번째 필드)
                //Toast.makeText(getApplicationContext(), note, Toast.LENGTH_LONG).show();

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
