package com.example.kkyubrother.busta_chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

public class DPushLogin extends Activity {

	private EditText mUsernameView;
	private EditText mUserageView;

	private String mUsername;
	private int mUserage;
	private int mUsergender = 1; // 1: 남성(기본), 2: 여성

	private boolean mIsBound = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chatlogin);

		// 로그인 폼을 설정한다.
		mUsernameView = (EditText) findViewById(R.id.username_input);
		mUserageView = (EditText) findViewById(R.id.userage_input);

		Button signInButton = (Button) findViewById(R.id.sign_in_button);
		signInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});

		int requestCode = getIntent().getIntExtra("requestcode", -1);

		if (requestCode == DPushMainFragment.REQUEST_UPDATE_USERINFO) {
			mUsernameView.setText(getIntent().getStringExtra("username"));
			mUserageView.setText(getIntent().getStringExtra("userage"));

			String usergender = getIntent().getStringExtra("usergender");
			RadioButton rbMale = (RadioButton) findViewById(R.id.radio_male);
			RadioButton rbFemale = (RadioButton) findViewById(R.id.radio_female);

			if ("1".equals(usergender)) rbMale.setChecked(true);
			else if ("2".equals(usergender)) rbFemale.setChecked(true);
		}
	}

	public void onRadioButtonClicked(View view) {
		boolean checked = ((RadioButton) view).isChecked();

		switch (view.getId()) {
		case R.id.radio_male:
			if (checked) mUsergender = 1;
			break;
		case R.id.radio_female:
			if (checked) mUsergender = 2;
			break;
		}
	}

	private void attemptLogin() {
		mUsernameView.setError(null);

		String username = mUsernameView.getText().toString().trim();

		// 유저아이디가 적합한지를 체크한다.
		if (TextUtils.isEmpty(username)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			mUsernameView.requestFocus();
			return;
		}

		String userage = mUserageView.getText().toString().trim();

		if (TextUtils.isEmpty(userage)) {
			mUserageView.setError(getString(R.string.error_field_required));
			mUserageView.requestFocus();
			return;
		}

		try {
			mUserage = Integer.parseInt(userage);
		}
		catch (Exception e) {
			mUserageView.setError(getString(R.string.error_age_number_required));
			mUserageView.requestFocus();
			return;
		}

		mUsername = username;
		Intent intent = new Intent();
		intent.putExtra("username", mUsername);
		intent.putExtra("userage", "" + mUserage);
		intent.putExtra("usergender", "" + mUsergender);
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
