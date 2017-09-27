package com.example.kkyubrother.busta_chat;

import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;

public class DPushActivity extends FragmentActivity {

	PowerManager powerManager;
	PowerManager.WakeLock wakeLock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chatmain);

		powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DPushChatWakelockTag");
		wakeLock.acquire();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		wakeLock.release();
	}
}
