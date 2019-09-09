package com.ale.filesharingdemo.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ale.filesharingdemo.R;
import com.ale.filesharingdemo.activities.StartupActivity;
import com.ale.filesharingdemo.adapters.ConversationsAdapter;
import com.ale.filesharingdemo.interfaces.RecyclerViewClickListener;
import com.ale.filesharingdemo.permissions.PermissionsHelper;
import com.ale.infra.http.adapter.concurrent.RainbowServiceException;
import com.ale.infra.manager.fileserver.IFileProxy;
import com.ale.infra.manager.fileserver.RainbowFileDescriptor;
import com.ale.infra.proxy.conversation.IRainbowConversation;
import com.ale.rainbowsdk.RainbowSdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConversationsFragment extends Fragment implements RecyclerViewClickListener, IFileProxy.IUploadFileListener {

    private StartupActivity activity;
    private SwipeRefreshLayout swipeRefreshView;
    private RecyclerView conversationsRecyclerView;
    private TextView uploadProgressionView;
    private List<IRainbowConversation> conversations = new ArrayList<>();
    private IRainbowConversation uploadFileConversation = null;

    private static int PICK_FILE_RESULT_CODE = 2;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (StartupActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(activity.getSupportActionBar() != null)
            activity.getSupportActionBar().setTitle(activity.getResources().getString(R.string.conversations));

        View fragmentView = inflater.inflate(R.layout.conversations_fragment, container, false);

        conversationsRecyclerView = fragmentView.findViewById(R.id.conversations_recycler);
        swipeRefreshView = fragmentView.findViewById(R.id.swipe_conversations);
        uploadProgressionView = fragmentView.findViewById(R.id.upload_percent);

        conversationsRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        conversationsRecyclerView.setAdapter(new ConversationsAdapter(conversations, activity, this));

        swipeRefreshView.setOnRefreshListener(this::getAllConversations);

        getAllConversations();

        return fragmentView;
    }

    @Override
    public void recyclerViewListClicked(View view, int position) {
        if(PermissionsHelper.instance().isExternalStorageAllowed(getContext(), activity)) {
            uploadFileConversation = conversations.get(position);

            if(uploadFileConversation != null) {
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
                chooseFile.setType("*/*");
                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(chooseFile, PICK_FILE_RESULT_CODE);
            }
        }
    }

    private void getAllConversations() {

        conversations.clear();
        conversations.addAll(RainbowSdk.instance().conversations().getAllConversations().getCopyOfDataList());

        activity.runOnUiThread(() -> {
            if(conversationsRecyclerView.getAdapter() != null)
                conversationsRecyclerView.getAdapter().notifyDataSetChanged();

            swipeRefreshView.setRefreshing(false);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_RESULT_CODE && resultCode == Activity.RESULT_OK) {

            if (data == null)
                return;

            activity.runOnUiThread(() -> swipeRefreshView.setRefreshing(true));
            Uri content_describer = data.getData();

            if (uploadFileConversation.isRoomType()) {
                RainbowSdk.instance().fileStorage()
                        .uploadFileToBubble(uploadFileConversation.getRoom(), content_describer, "File Upload", this);
            } else {
                RainbowSdk.instance().fileStorage()
                        .uploadFileToConversation(uploadFileConversation, content_describer, "File upload", this);
            }
        }
    }

    @Override
    public void onUploadSuccess(RainbowFileDescriptor rainbowFileDescriptor) {
        activity.runOnUiThread(() -> {
            Toast.makeText(getActivity(), "File uploaded", Toast.LENGTH_SHORT).show();
            uploadProgressionView.setVisibility(View.INVISIBLE);
            swipeRefreshView.setRefreshing(false);
        });
        activity.openFilesFragment();
    }

    @Override
    public void onUploadInProgress(int i) {
        activity.runOnUiThread(() -> {
            uploadProgressionView.setVisibility(View.VISIBLE);
            uploadProgressionView.setText(String.format(Locale.getDefault(), "%d %%", i));
        });
    }

    @Override
    public void onUploadFailed(RainbowFileDescriptor rainbowFileDescriptor, RainbowServiceException e) {
        activity.runOnUiThread(() -> {
            Toast.makeText(getActivity(), "Error uploading file", Toast.LENGTH_SHORT).show();
            uploadProgressionView.setVisibility(View.INVISIBLE);
            swipeRefreshView.setRefreshing(false);
        });
    }
}
