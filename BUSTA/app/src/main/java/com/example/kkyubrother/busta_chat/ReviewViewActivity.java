package com.example.kkyubrother.busta_chat;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import static com.example.kkyubrother.busta_chat.R.id.btn_back;
import static com.example.kkyubrother.busta_chat.R.layout.review_view;

/**
 * Created by genie on 2016-10-28.
 */

public class ReviewViewActivity extends AppCompatActivity {

    private Cursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(review_view);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");

        TextView tv_name = (TextView)findViewById(R.id.tv_name);
        TextView tv_title = (TextView)findViewById(R.id.tv_title);
        TextView tv_content = (TextView)findViewById(R.id.tv_content);

        tv_name.setText(name);
        tv_title.setText(title);
        tv_content.setText(content);
    }

    public void onClick(View view) {

        switch(view.getId()){
            case btn_back:
                Intent it = new Intent(this, ReviewActivity.class);
                startActivity(it);
                finish();
                break;
        }
    }

}
