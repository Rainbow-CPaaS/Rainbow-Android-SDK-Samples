package com.ale.filesharingdemo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ale.filesharingdemo.R;
import com.ale.filesharingdemo.interfaces.RecyclerViewClickListener;
import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.manager.fileserver.RainbowFileDescriptor;
import com.ale.rainbowsdk.RainbowSdk;

import java.util.List;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileViewHolder> {

    private List<RainbowFileDescriptor> filesList;
    private ImageView downloadView;
    private ImageView deleteView;
    private RecyclerViewClickListener itemListener;

    public FilesAdapter(List<RainbowFileDescriptor> filesList, RecyclerViewClickListener itemListener) {
        this.filesList = filesList;
        this.itemListener = itemListener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.files_row, parent, false);
        downloadView = rootView.findViewById(R.id.ic_download);
        deleteView = rootView.findViewById(R.id.ic_delete);
        return new FileViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder filesViewHolder, int position) {
        filesViewHolder.bind(filesList.get(position));
        filesViewHolder.setIsRecyclable(false);
    }

    @Override
    public int getItemCount() {
        return filesList.size();
    }

    class FileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        FileViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setTag(this);
            downloadView.setOnClickListener(this);
            deleteView.setOnClickListener(this);
        }

        void bind(RainbowFileDescriptor file) {
            String name = file.getFileName();
            IRainbowContact filePossessor = RainbowSdk.instance().contacts().getContactFromCorporateId(file.getOwnerId());

            boolean isSentByMe = filePossessor != null && RainbowSdk.instance().myProfile().getConnectedUser().getImJabberId().equals(
                    filePossessor.getImJabberId());

            if (isSentByMe) {
                deleteView.setVisibility(View.VISIBLE);
            } else {
                deleteView.setVisibility(View.INVISIBLE);
            }

            if (RainbowSdk.instance().fileStorage().isDownloaded(file)) {
                downloadView.setVisibility(View.INVISIBLE);
            } else {
                downloadView.setVisibility(View.VISIBLE);
            }

            ((TextView) itemView.findViewById(R.id.file_name)).setText(name);
        }

        @Override
        public void onClick(View view) {
            itemListener.recyclerViewListClicked(view, getLayoutPosition());
        }


    }
}
