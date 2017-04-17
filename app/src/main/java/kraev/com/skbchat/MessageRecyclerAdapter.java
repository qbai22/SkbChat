package kraev.com.skbchat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import kraev.com.skbchat.model.ChatMessage;
import kraev.com.skbchat.utils.UserPreferencesUtils;

/**
 * Поскольку нам нужно выводить на экран сообщения
 * и слева и справа, слева - сообщения активного пользователя
 * а справа сообщения других участников чата:
 * то используется два лэйаута в зависимости от
 * параметра сообщения
 */

public class MessageRecyclerAdapter extends RecyclerView.Adapter<MessageRecyclerAdapter.SenderMessageViewHolder> {

    private final static int SENDER_LAYOUT_ID = 11;
    private final static int RECIEVER_LAYOUT_ID = 12;

    List<ChatMessage> messagesData;
    Context mContext;


    public MessageRecyclerAdapter(List<ChatMessage> data, Context context) {
        messagesData = data;
        mContext = context;
    }

    @Override
    public SenderMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == SENDER_LAYOUT_ID) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sender, parent, false);
            return new SenderMessageViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_reciever, parent, false);
            return new SenderMessageViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(SenderMessageViewHolder holder, int position) {
        ChatMessage message = messagesData.get(position);
        String photoUrl = message.getPhotoUrl();
        if (photoUrl != null) {
            holder.sPhotoImageView.setVisibility(View.VISIBLE);
            holder.sMessageText.setVisibility(View.GONE);
            Glide.with(mContext)
                    .load(photoUrl)
                    .into(holder.sPhotoImageView);
        } else {
            holder.sPhotoImageView.setVisibility(View.GONE);
            holder.sMessageText.setVisibility(View.VISIBLE);
            String text = message.getText();
            holder.sMessageText.setText(text);
        }
        String nickName = message.getName();
        holder.sNickName.setText(nickName);
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messagesData.get(position);
        //если это сообщение текущего пользователя
        //возвращаем идентификатор соответствующего холдера
        if (isSenderReads(message)) {
            return SENDER_LAYOUT_ID;
        } else {
            return RECIEVER_LAYOUT_ID;
        }
    }

    @Override
    public int getItemCount() {
        return messagesData.size();
    }

    private boolean isSenderReads(ChatMessage message) {
        String currentUserUid = UserPreferencesUtils.getCurrentUserUid(mContext);
        return message.getSenderUid().equals(currentUserUid);
    }

    class SenderMessageViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_nick_name_text_view)
        TextView sNickName;
        @BindView(R.id.item_msg_text_view)
        TextView sMessageText;
        @BindView(R.id.item_photo_image_view)
        ImageView sPhotoImageView;

        SenderMessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


}
