package com.ale.conversationsDemo.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ale.infra.http.GetFileResponse;
import com.ale.infra.manager.fileserver.IFileProxy;
import com.ale.infra.manager.fileserver.RainbowFileDescriptor;
import com.ale.infra.manager.room.Room;
import com.ale.infra.proxy.conversation.IRainbowConversation;
import com.ale.rainbowsdk.FileStorage;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.conversationsDemo.R;
import com.ale.conversationsDemo.activity.StartupActivity;

import java.io.File;
import java.util.List;

public class SharedFilesAdapter extends BaseAdapter {

    private List<RainbowFileDescriptor> m_listToDisplay = null;
    private FileStorage.GetMode m_mode;

    private Context m_context;

    private Room m_room = null;
    private IRainbowConversation m_conversation = null;

    public SharedFilesAdapter(Context context, FileStorage.GetMode mode) {
        m_context = context;
        m_mode = mode;

        if (m_conversation == null && m_room == null) {
            updateListToDisplay();
        }
    }

    public SharedFilesAdapter(Context context, FileStorage.GetMode mode, IRainbowConversation conversation) {
        this(context, mode);
        m_conversation = conversation;
        updateListToDisplay();
    }

    public SharedFilesAdapter(Context context, FileStorage.GetMode mode, Room room) {
        this(context, mode);
        m_room = room;
        updateListToDisplay();
    }

    public void setMode(FileStorage.GetMode mode) {
        m_mode = mode;
    }

    public void setConversation(IRainbowConversation conversation) {
        m_conversation = conversation;
    }

    public void setRoom(Room room) {
        m_room = room;
    }

    public void updateListToDisplay() {
        if (m_mode.isSentMode()) {
            if (m_conversation != null) {
                m_listToDisplay = RainbowSdk.instance().fileStorage().getFilesSentInConversation(m_conversation);
            } else if (m_room != null) {
                m_listToDisplay = RainbowSdk.instance().fileStorage().getFilesSentInBubble(m_room);
            } else {
                m_listToDisplay = RainbowSdk.instance().fileStorage().getAllFilesSent().getCopyOfDataList();
            }
        } else {
            if (m_conversation != null) {
                m_listToDisplay = RainbowSdk.instance().fileStorage().getFilesReceivedInConversation(m_conversation);
            } else if (m_room != null) {
                m_listToDisplay = RainbowSdk.instance().fileStorage().getFilesReceivedInBubble(m_room);
            } else {
                m_listToDisplay = RainbowSdk.instance().fileStorage().getAllFilesReceived().getCopyOfDataList();
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return m_listToDisplay.size();
    }

    @Override
    public Object getItem(int position) {
        return m_listToDisplay.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MyViewHolder myViewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)m_context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_list_shared_files, parent, false);

            myViewHolder = new MyViewHolder();
            myViewHolder.icon = (ImageView)convertView.findViewById(R.id.icon);
            myViewHolder.title = (TextView)convertView.findViewById(R.id.title);
            myViewHolder.size = (TextView)convertView.findViewById(R.id.size);

            convertView.setTag(myViewHolder);
        } else {
            myViewHolder = (MyViewHolder)convertView.getTag();
        }

        final RainbowFileDescriptor fileDescriptor = (RainbowFileDescriptor)getItem(position);

        if (fileDescriptor.getImage() != null) {
            myViewHolder.icon.setImageBitmap(fileDescriptor.getImage());
        } else {
            myViewHolder.icon.setImageResource(R.drawable.unknown_image);
        }
        myViewHolder.title.setText(fileDescriptor.getFileName());
        myViewHolder.size.setText(fileDescriptor.getSize()/1000 + " Ko");

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                RainbowSdk.instance().fileStorage().removeFile(fileDescriptor, new IFileProxy.IDeleteFileListener() {
                    @Override
                    public void onDeletionSuccess() {
                        if (m_context instanceof StartupActivity) {
                            ((StartupActivity) m_context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(m_context, "File removed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onDeletionError() {

                    }
                });
                return true;
            }
        });

        myViewHolder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File fileDownloaded = RainbowSdk.instance().fileStorage().getFileDownloaded(fileDescriptor);

                if (fileDownloaded == null) {
                    final ProgressDialog progressDialog = new ProgressDialog(m_context);
                    progressDialog.setMessage("Downloading file");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.show();
                    RainbowSdk.instance().fileStorage().downloadFile(fileDescriptor, new IFileProxy.IDownloadFileListener() {
                        @Override
                        public void onDownloadSuccess(GetFileResponse result) {
                            progressDialog.dismiss();
                            ((StartupActivity)m_context).openFile(fileDescriptor);
                        }

                        @Override
                        public void onDownloadInProgress(final GetFileResponse result) {
                            progressDialog.setProgress(result.getPercentDownloaded());
                        }

                        @Override
                        public void onDownloadFailed(boolean notFound) {
                            progressDialog.dismiss();
                        }
                    });
                } else {
                    ((StartupActivity)m_context).openFile(fileDescriptor);
                }
            }
        });

        return convertView;
    }

    private class MyViewHolder {
        ImageView icon;
        TextView title;
        TextView size;
    }
}
