package com.example.kkyubrother.busta_chat;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DPushMessageAdapter extends RecyclerView.Adapter<DPushMessageAdapter.ViewHolder> {

	private List<DPushMessage> mMessages;

	private int[] mUsernameColors;

	public DPushMessageAdapter(Context context, List<DPushMessage> messages) {
		mMessages = messages;
		mUsernameColors = context.getResources().getIntArray(R.array.username_colors);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		int layout = -1;
		switch (viewType) {
		case DPushMessage.TYPE_MESSAGE:
			layout = R.layout.item_message;
			break;
		case DPushMessage.TYPE_LOG:
			layout = R.layout.item_log;
			break;
		case DPushMessage.TYPE_ACTION:
			layout = R.layout.item_action;
			break;
		}
		View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int position) {
		DPushMessage message = mMessages.get(position);
		viewHolder.setMessage(message.getMessage());
		viewHolder.setUsername(message.getUsername());
	}

	@Override
	public int getItemCount() {
		return mMessages.size();
	}

	@Override
	public int getItemViewType(int position) {
		return mMessages.get(position).getType();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		private TextView mUsernameView;
		private TextView mMessageView;

		public ViewHolder(View itemView) {
			super(itemView);

			mUsernameView = (TextView) itemView.findViewById(R.id.username);
			mMessageView = (TextView) itemView.findViewById(R.id.message);
		}

		public void setUsername(String username) {
			if (null == mUsernameView) return;
			mUsernameView.setText(username);
			mUsernameView.setTextColor(getUsernameColor(username));
		}

		public void setMessage(String message) {
			if (null == mMessageView) return;
			mMessageView.setText(message);
		}

		private int getUsernameColor(String username) {
			int hash = 7;
			for (int i = 0, len = username.length(); i < len; i++) {
				hash = username.codePointAt(i) + (hash << 5) - hash;
			}
			int index = Math.abs(hash % mUsernameColors.length);
			return mUsernameColors[index];
		}
	}
}
