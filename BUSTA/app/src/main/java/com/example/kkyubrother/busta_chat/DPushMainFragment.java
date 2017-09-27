package com.example.kkyubrother.busta_chat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import kr.co.dpush.client.DPClient;
import kr.co.dpush.client.DPOptions;
import kr.co.dpush.client.GroupInfo;
import kr.co.dpush.client.GroupOptions;
import kr.co.dpush.common.Callback;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class DPushMainFragment extends Fragment {

	public static final String PRODUCT_KEY = "0bc088c0fb9ae3bbb30994";

	public static final int REQUEST_LOGIN = 0;
	public static final int REQUEST_UPDATE_USERINFO = 1;
	public static final int REQUEST_GET_PRODKEY = 2;

	private static final String TAG = DPushMainFragment.class.getSimpleName();
	private static final int TYPING_TIMER_LENGTH = 600;
	private static final int ACTION_SHOW_TEXTMESSAGE = 0x3500;

	private static final int ACTION_ON_CONNECT = 0x3001;
	private static final int ACTION_ON_USERIN = 0x5002;
	private static final int ACTION_ON_USEROUT = 0x5003;
	private static final int ACTION_ON_USERUPDATED = 0x5004;
	private static final int ACTION_ON_GROUPOPENED = 0x3003;

	private RecyclerView mMessagesView;
	private EditText mInputMessageView;
	private List<DPushMessage> mMessages = new ArrayList<DPushMessage>();
	private RecyclerView.Adapter mAdapter;
	private boolean mTyping = false;
	private Handler mTypingHandler = new Handler();
	private String mUsername = "";
	private String mUserage = "";
	private String mUsergender = "";
	private boolean mIsBound = false;
	Messenger mService = null;
	private DPClient client;
	private Handler mHandler;
	private String mProdKey = "";
	private boolean mProdKeyChecked = false;

	public DPushMainFragment() {
		super();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mAdapter = new DPushMessageAdapter(context, mMessages);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(false);

		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				Log.d(TAG, "in mHandler");
				Bundle b;
				switch (msg.what) {
				case ACTION_ON_CONNECT:
					b = msg.getData();
					String errMessage = b.getString("errmessage");
					Log.d(TAG, "in ACTION_ON_CONNECT");
					if (errMessage.isEmpty()) {
						if (!mProdKeyChecked) {
							// Product key를 store한다
							SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = sharedPref.edit();
							editor.putString("PRODUCT_KEY", mProdKey);
							editor.commit();
							mProdKeyChecked = true;

							startSignIn();
						}
					}
					else {
						if (!mProdKeyChecked) {
							if (client != null) client.close();
							startGetProdKey(errMessage);
						}
						mProdKeyChecked = true;

					}
					break;
				case ACTION_ON_GROUPOPENED:
					showUserList();
					break;
				case ACTION_SHOW_TEXTMESSAGE:
					b = msg.getData();
					addMessage(b.getString("nickname"), b.getString("message"));
					break;
				case ACTION_ON_USERIN:
					b = msg.getData();
					addLog("'" + b.getString("nickname") + "(" + b.getString("age") + ")'님이 입장하셨습니다.");
					break;
				case ACTION_ON_USEROUT:
					b = msg.getData();
					addLog("'" + b.getString("nickname") + "(" + b.getString("age") + ")'님이 퇴장하셨습니다.");
					break;
				case ACTION_ON_USERUPDATED:
					b = msg.getData();
					addLog("'" + b.getString("nickname") + "(" + b.getString("age") + ")'님으로 대화명이 변경되었습니다.");
					break;
				default:
					super.handleMessage(msg);
				}
			};
		};

		// 이전에 설정된 Product key를 찾는다.
		SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
		mProdKey = sharedPref.getString("PRODUCT_KEY", PRODUCT_KEY);
//        mProdKey = PRODUCT_KEY;


		connectDPClient();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_main, container, false);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (client != null) client.close();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mMessagesView = (RecyclerView) view.findViewById(R.id.messages);
		mMessagesView.setLayoutManager(new LinearLayoutManager(getActivity()));
		mMessagesView.setAdapter(mAdapter);

		mInputMessageView = (EditText) view.findViewById(R.id.message_input);

		ImageButton sendButton = (ImageButton) view.findViewById(R.id.send_button);
		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				attemptSend();
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		if (Activity.RESULT_OK != resultCode) {
			getActivity().finish();
			return;
		}

		if (REQUEST_GET_PRODKEY == requestCode) {
			if (mProdKey.equals(data.getStringExtra("prodkey"))) return;

			mProdKey = data.getStringExtra("prodkey");
			connectDPClient();
		}
		else if (REQUEST_LOGIN == requestCode) {
			mUsername = data.getStringExtra("username");
			mUserage = data.getStringExtra("userage");
			mUsergender = data.getStringExtra("usergender");

			removeAllMessages();
			addLog(getResources().getString(R.string.message_welcome));
			performOpenGroup();
		}
		else if (REQUEST_UPDATE_USERINFO == requestCode) {

			if (mUsername.equals(data.getStringExtra("username")) && mUserage.equals(data.getStringExtra("userage")) && mUsergender.equals(data.getStringExtra("usergender"))) return;

			mUsername = data.getStringExtra("username");
			mUserage = data.getStringExtra("userage");
			mUsergender = data.getStringExtra("usergender");
			update_userinfo();
		}
	}

	private void connectDPClient() {
		if (client != null) {
			client.close();
			client = null;
			gi = null;
		}

		mProdKeyChecked = false;

		DPOptions dpopts = new DPOptions();
		dpopts.setResultcallback(actionOnConnect(mHandler));

		client = new DPClient(mProdKey, dpopts) {

			public void onConnected() {
				Log.d(TAG, "onconnected................");
			}

			public void onDisconnected() {
				Log.d(TAG, "onDisconnected................");
				if (!client.isConnected()) client.connect();
			}
		};

		client.connect();
	}

	private String GRP_ID = "chat-group";
	private String ACT_ID = "chat-action";
	private GroupInfo gi;

	public void performOpenGroup() {

		// 오픈된 그룹이면 정의된 ActionID 에 해당하는 callback만을 재정의한다
		if (client.isOpened(GRP_ID)) {
			gi = client.getOpenedGroup(GRP_ID);
			gi.onReceive(ACT_ID, action(mHandler));
			gi.setCallback(actionOnGroupOpened(mHandler));
			gi.setOnUserIn(actionOnUserIn(mHandler));
			gi.setOnUserOut(actionOnUserOut(mHandler));
			gi.setOnUserUpdated(actionOnUserUpdated(mHandler));
		}
		// 그룹을 오픈한다
		else {
			GroupOptions gpopts = new GroupOptions();
			gpopts.setCustevent(true);
			gpopts.setSendevent(true);

			JSONObject cinfo = new JSONObject();
			try {
				cinfo.put("nickname", mUsername);
				cinfo.put("age", mUserage);
				if ("1".equals(mUsergender)) cinfo.put("gender", "M");
				else if ("2".equals(mUsergender)) cinfo.put("gender", "F");
				else cinfo.put("gender", "P");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			gpopts.setCustinfo(cinfo);

			gi = client.openGroup(GRP_ID, gpopts, null);

			// 메시지가 수신될때 수행되는 callback 함수 등록
			gi.onReceive(ACT_ID, action(mHandler));
			// Group 오픈 요청에 대한 결과를 처리하는 callback 함수 등록
			gi.setCallback(actionOnGroupOpened(mHandler));
			// 채팅방에 사용자가 들어올때 수행되는 callback 함수 등록
			gi.setOnUserIn(actionOnUserIn(mHandler));
			// 채팅방에서 사용자가 나갈때 수행되는 callback 함수 등록
			gi.setOnUserOut(actionOnUserOut(mHandler));
			// 채팅방내 사용자정보가 변경될때 수행되는 callback 함수 등록
			gi.setOnUserUpdated(actionOnUserUpdated(mHandler));
		}
	}

	public void update_userinfo() {
		JSONObject cinfo = new JSONObject();
		try {
			cinfo.put("nickname", mUsername);
			cinfo.put("age", mUserage);
			if ("1".equals(mUsergender)) cinfo.put("gender", "M");
			else if ("2".equals(mUsergender)) cinfo.put("gender", "F");
			else cinfo.put("gender", "P");
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		gi.updateUser(cinfo);
	}

	public void sendTextMessage(String textMessage) {
		if (mProdKey.isEmpty()) {
			startGetProdKey("");
		}
		gi.send(ACT_ID, textMessage);
		return;
	}

	private static Callback actionOnConnect(final Handler handler) {
		return new Callback() {
			@Override
			public void call(Object... args) {
				String errMessage = (String) args[1];
				Message msg = Message.obtain(null, ACTION_ON_CONNECT, 0, 0);
				Bundle b = new Bundle();
				b.putString("errmessage", errMessage);
				msg.setData(b);

				handler.sendMessage(msg);
			}
		};
	}

	private static Callback action(final Handler handler) {
		return new Callback() {
			@Override
			public void call(Object... args) {
				String message = (String) args[0];
				JSONObject custInfo = (JSONObject) args[2];
				String nickname;
				try {
					nickname = custInfo.getString("nickname");
				}
				catch (JSONException e) {
					return;
				}

				Message msg = Message.obtain(null, ACTION_SHOW_TEXTMESSAGE, 0, 0);
				Bundle b = new Bundle();
				b.putString("nickname", nickname);
				b.putString("message", message);
				msg.setData(b);

				if (message != null) {
					handler.sendMessage(msg);
				}
			}
		};
	}

	private static Callback actionOnUserIn(final Handler handler) {
		return new Callback() {
			@Override
			public void call(Object... args) {

				JSONObject custInfo = (JSONObject) args[1];
				String nickname, age;
				try {
					nickname = custInfo.getString("nickname");
					age = custInfo.getString("age");
				}
				catch (JSONException e) {
					return;
				}

				Message msg = Message.obtain(null, ACTION_ON_USERIN, 0, 0);
				Bundle b = new Bundle();
				b.putString("nickname", nickname);
				b.putString("age", age);
				msg.setData(b);

				if (nickname != null && !nickname.isEmpty()) {
					handler.sendMessage(msg);
				}
			}
		};
	}

	private static Callback actionOnUserOut(final Handler handler) {
		return new Callback() {
			@Override
			public void call(Object... args) {

				JSONObject custInfo = (JSONObject) args[1];
				String nickname, age;
				try {
					nickname = custInfo.getString("nickname");
					age = custInfo.getString("age");
				}
				catch (JSONException e) {
					return;
				}

				Message msg = Message.obtain(null, ACTION_ON_USEROUT, 0, 0);
				Bundle b = new Bundle();
				b.putString("nickname", nickname);
				b.putString("age", age);
				msg.setData(b);

				if (nickname != null && !nickname.isEmpty()) {
					handler.sendMessage(msg);
				}
			}
		};
	}

	private static Callback actionOnUserUpdated(final Handler handler) {
		return new Callback() {
			@Override
			public void call(Object... args) {

				JSONObject custInfo = (JSONObject) args[1];
				String nickname, age;
				try {
					nickname = custInfo.getString("nickname");
					age = custInfo.getString("age");
				}
				catch (JSONException e) {
					return;
				}

				Message msg = Message.obtain(null, ACTION_ON_USERUPDATED, 0, 0);
				Bundle b = new Bundle();
				b.putString("nickname", nickname);
				b.putString("age", age);
				msg.setData(b);

				if (nickname != null && !nickname.isEmpty()) {
					handler.sendMessage(msg);
				}
			}
		};
	}

	private static Callback actionOnGroupOpened(final Handler handler) {
		return new Callback() {
			@Override
			public void call(Object... args) {
				Message msg = Message.obtain(null, ACTION_ON_GROUPOPENED, 0, 0);
				handler.sendMessage(msg);
			}
		};
	}

	// 현재 접속된 사용자들의 목록을 화면에 출력
	private void showUserList() {
		JSONObject userList;
		userList = gi.getUserList();
		String userListResult = "";

		int i = 0;
		try {
			Iterator<String> keys = userList.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				JSONObject custInfo = userList.getJSONObject(key);

				String genderResult = "";

				if (custInfo.has("gender")) {
					Log.d("DPushMainFragment", "gender" + custInfo.getString("gender"));
					genderResult = custInfo.getString("gender");
				}

				if ("M".equals(genderResult)) genderResult = "남성";
				else if ("F".equals(genderResult)) genderResult = "여성";
				else genderResult = "";

				userListResult += "'" + custInfo.getString("nickname") + "(" + genderResult + "," + custInfo.getString("age") + ")'";
				if (i < userList.length() - 1) userListResult += ", ";
				i++;
			}
			addLog("현재 접속된 사용자는 " + userListResult + " 입니다.");
		}
		catch (JSONException e) {
			addLog("ERROR: 현재 접속된 사용자는 " + userListResult + " 입니다.");
		}

		return;
	}

	private void addLog(String message) {
		mMessages.add(new DPushMessage.Builder(DPushMessage.TYPE_LOG).message(message).build());
		mAdapter.notifyItemInserted(mMessages.size() - 1);
		scrollToBottom();
	}

	private void addMessage(String username, String message) {
		mMessages.add(new DPushMessage.Builder(DPushMessage.TYPE_MESSAGE).username(username).message(message).build());
		mAdapter.notifyItemInserted(mMessages.size() - 1);
		scrollToBottom();
	}

	private void addTyping(String username) {
		mMessages.add(new DPushMessage.Builder(DPushMessage.TYPE_ACTION).username(username).build());
		mAdapter.notifyItemInserted(mMessages.size() - 1);
		scrollToBottom();
	}

	private void removeTyping(String username) {
		for (int i = mMessages.size() - 1; i >= 0; i--) {
			DPushMessage message = mMessages.get(i);
			if (message.getType() == DPushMessage.TYPE_ACTION && message.getUsername().equals(username)) {
				mMessages.remove(i);
				mAdapter.notifyItemRemoved(i);
			}
		}
	}

	private void removeAllMessages() {
		for (int i = mMessages.size() - 1; i >= 0; i--) {
			mMessages.remove(i);
			mAdapter.notifyItemRemoved(i);
		}
	}
//	메세지 전송 함수
//	명령어 사용을 전제로 변형
	private void attemptSend() {
		if (null == mUsername) return;
		mTyping = false;

		String message = mInputMessageView.getText().toString().trim();
		if (TextUtils.isEmpty(mInputMessageView.getText())) {
			mInputMessageView.requestFocus();
			return;
		}
		if (TextUtils.equals(mInputMessageView.getText(), "/help")) {
			addLog("----------------명령어----------------");
			addLog("/help : 도움말을 출력합니다.");
//			addLog("/prodkey : Product_Key를 변경합니다");
			addLog("/showuser : 접속중인 유저를 표시합니다");
			addLog("/cguser : 닉네님을 변경합니다");
			addLog("/clear : 현재의 채팅 메세지를 지웁니다");
			addLog("--------------------------------------");
			mInputMessageView.setText("");
			return;
//		} else if (TextUtils.equals(mInputMessageView.getText(), "/prodkey")) {
//			startGetProdKey("");
//			mInputMessageView.setText("");
//			return;
		} else if (TextUtils.equals(mInputMessageView.getText(), "/showuser")) {
			showUserList();
			mInputMessageView.setText("");
			return;
		} else if (TextUtils.equals(mInputMessageView.getText(), "/cguser")) {
			startUpdateUserinfo();
			mInputMessageView.setText("");
			return;
		} else if (TextUtils.equals(mInputMessageView.getText(), "/clear")) {
			removeAllMessages();
			mInputMessageView.setText("");
			return;
		}

		mInputMessageView.setText("");

		// 메시지를 전송한다.
		sendTextMessage(message);
	}

	private void startGetProdKey(String errMessage) {
		Intent intent = new Intent(getActivity(), DPushProdKeyCheck.class);
		intent.putExtra("requestcode", REQUEST_GET_PRODKEY);
		intent.putExtra("prodkey", mProdKey);
		intent.putExtra("errmessage", errMessage);
		startActivityForResult(intent, REQUEST_GET_PRODKEY);
	}

	// Login 정보 설정을 위해 Login 창을 띄운다.
	private void startSignIn() {
		mUsername = "";
		Intent intent = new Intent(getActivity(), DPushLogin.class);
		intent.putExtra("requestcode", REQUEST_LOGIN);
		startActivityForResult(intent, REQUEST_LOGIN);
	}

	// Login 정보 변경을 위해 Login 창을 띄운다.
	private void startUpdateUserinfo() {
		Intent intent = new Intent(getActivity(), DPushLogin.class);
		intent.putExtra("username", mUsername);
		intent.putExtra("userage", "" + mUserage);
		intent.putExtra("usergender", "" + mUsergender);
		intent.putExtra("requestcode", REQUEST_UPDATE_USERINFO);
		startActivityForResult(intent, REQUEST_UPDATE_USERINFO);
	}

	private void scrollToBottom() {
		mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
	}
}
