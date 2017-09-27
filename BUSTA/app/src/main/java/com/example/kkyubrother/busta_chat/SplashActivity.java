package com.example.kkyubrother.busta_chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;


/**
 * Created by genie on 2016-10-25.
 */

public class SplashActivity extends AppCompatActivity {

    Handler handler = new Handler();
    Runnable r = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView iv_splash = (ImageView) findViewById(R.id.iv_splash);

        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(iv_splash);
        Glide.with(this).load(R.raw.loading7).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(iv_splash);
    }

        @Override
        protected void onResume() {
            super. onResume();
            handler.postDelayed(r,3500);
        }

        @Override
        protected void onPause() {
            super. onPause();
            handler.removeCallbacks(r);
        }
}
