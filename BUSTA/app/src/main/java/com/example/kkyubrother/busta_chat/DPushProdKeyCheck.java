package com.example.kkyubrother.busta_chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class DPushProdKeyCheck extends Activity {

	private EditText mProdKeyView;
	private String mProdKey;
	private boolean mIsBound = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_prodkeycheck);

		Log.d("DPushProdKeyCheck", "onCreate");
		mProdKeyView = (EditText) findViewById(R.id.prodkey_input);

		Button prodKeyInButton = (Button) findViewById(R.id.prodkey_in_button);
		prodKeyInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				setProdKey();
			}
		});

		mProdKey = getIntent().getStringExtra("prodkey");
		mProdKeyView.setText(getIntent().getStringExtra("prodkey"));

		if (!getIntent().getStringExtra("errmessage").isEmpty()) mProdKeyView.setError(getIntent().getStringExtra("errmessage"));
	}

	private void setProdKey() {
		mProdKeyView.setError(null);
		String prodkey = mProdKeyView.getText().toString().trim();

		// 유저아이디가 적합한지를 체크한다.
		if (TextUtils.isEmpty(prodkey)) {
			mProdKeyView.setError(getString(R.string.error_field_required));
			mProdKeyView.requestFocus();
			return;
		}

		Intent intent = new Intent();
		intent.putExtra("prodkey", prodkey);
		setResult(RESULT_OK, intent);
		finish();
	}
}
