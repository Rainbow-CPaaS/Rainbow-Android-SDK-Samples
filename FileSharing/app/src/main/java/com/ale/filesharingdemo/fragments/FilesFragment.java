package com.ale.filesharingdemo.fragments;

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
import com.ale.filesharingdemo.adapters.FilesAdapter;
import com.ale.filesharingdemo.interfaces.RecyclerViewClickListener;
import com.ale.filesharingdemo.permissions.PermissionsHelper;
import com.ale.infra.http.GetFileResponse;
import com.ale.infra.manager.fileserver.IFileProxy;
import com.ale.infra.manager.fileserver.RainbowFileDescriptor;
import com.ale.infra.proxy.fileserver.Consumption;
import com.ale.rainbowsdk.RainbowSdk;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FilesFragment extends Fragment implements RecyclerViewClickListener {

    private StartupActivity activity;
    private SwipeRefreshLayout swipeRefreshView;
    private RecyclerView filesRecyclerView;
    private TextView consumptionView;
    private TextView dlProgressionView;
    private List<RainbowFileDescriptor> files = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (StartupActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(activity.getSupportActionBar() != null)
            activity.getSupportActionBar().setTitle(activity.getResources().getString(R.string.files));

        View fragmentView = inflater.inflate(R.layout.files_fragment, container, false);

        filesRecyclerView = fragmentView.findViewById(R.id.file_recycler);
        swipeRefreshView = fragmentView.findViewById(R.id.swipe_files);
        consumptionView = fragmentView.findViewById(R.id.consumption);
        dlProgressionView = fragmentView.findViewById(R.id.download_percent);
        FloatingActionButton fabView = fragmentView.findViewById(R.id.fab_add_file);

        filesRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        filesRecyclerView.setAdapter(new FilesAdapter(files, this));

        swipeRefreshView.setOnRefreshListener(() ->  {
            refreshFilesList();
        });

        fabView.setOnClickListener(view -> activity.openConversationsFragment());

        refreshFilesList();

        return fragmentView;
    }

    private void refreshFilesList() {
        getConsumption();
        getAllFiles();
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {
        switch(v.getId()) {
            case R.id.ic_download:
                if(PermissionsHelper.instance().isExternalStorageAllowed(getContext(), activity)) {
                    RainbowSdk.instance().fileStorage().downloadFile(files.get(position), new IFileProxy.IDownloadFileListener() {
                        @Override
                        public void onDownloadSuccess(GetFileResponse getFileResponse) {
                            activity.runOnUiThread(() -> {
                                Toast.makeText(getActivity(), "File downloaded in /storage/download", Toast.LENGTH_SHORT).show();
                                dlProgressionView.setVisibility(View.INVISIBLE);
                            });
                            getAllFiles();
                        }

                        @Override
                        public void onDownloadInProgress(GetFileResponse getFileResponse) {
                            int percentDownloaded = getFileResponse.getPercentDownloaded();
                            activity.runOnUiThread(() -> {
                                dlProgressionView.setVisibility(View.VISIBLE);
                                dlProgressionView.setText(String.format(Locale.getDefault(),"%d %%", percentDownloaded));
                            });
                        }

                        @Override
                        public void onDownloadFailed(boolean b) {
                            activity.runOnUiThread(() -> {
                                Toast.makeText(getActivity(), "Error downloading file", Toast.LENGTH_SHORT).show();
                                dlProgressionView.setVisibility(View.INVISIBLE);
                            });

                        }
                    });
                }
                break;

            case R.id.ic_delete:
                RainbowSdk.instance().fileStorage().removeFile(files.get(position), new IFileProxy.IDeleteFileListener() {
                    @Override
                    public void onDeletionSuccess() {
                        files.remove(position);
                        if(filesRecyclerView.getAdapter() != null)
                            activity.runOnUiThread(() -> filesRecyclerView.getAdapter().notifyDataSetChanged());

                        getConsumption();
                    }

                    @Override
                    public void onDeletionError() {
                        activity.runOnUiThread(() -> Toast.makeText(getActivity(), "Error deleting file", Toast.LENGTH_SHORT).show());
                    }
                });
                break;
        }
    }

    private void getAllFiles() {
        files.clear();
        RainbowSdk.instance().fileStorage().fetchAllFilesSent(new IFileProxy.IRefreshListener() {
            @Override
            public void onRefreshSuccess(List<RainbowFileDescriptor> fileDescriptorList) {

                files.addAll(fileDescriptorList);

                RainbowSdk.instance().fileStorage().fetchAllFilesReceived(new IFileProxy.IRefreshListener() {
                    @Override
                    public void onRefreshSuccess(List<RainbowFileDescriptor> fileDescriptorList) {
                        files.addAll(fileDescriptorList);
                        activity.runOnUiThread(() -> {
                            if(filesRecyclerView.getAdapter() != null)
                                filesRecyclerView.getAdapter().notifyDataSetChanged();

                            swipeRefreshView.setRefreshing(false);
                        });
                    }

                    @Override
                    public void onRefreshFailed() {
                        activity.runOnUiThread(() -> {
                            Toast.makeText(getActivity(), "Error retrieving files received", Toast.LENGTH_SHORT).show();
                            swipeRefreshView.setRefreshing(false);
                        });
                    }
                });
            }

            @Override
            public void onRefreshFailed() {
                activity.runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Error retrieving files sent", Toast.LENGTH_SHORT).show();
                    swipeRefreshView.setRefreshing(false);
                });
            }
        });
    }

    private void getConsumption() {
        RainbowSdk.instance().fileStorage().getUserQuotaConsumption(new IFileProxy.IGetConsumptionListener() {
            @Override
            public void onGetSuccess(Consumption consumption) {
                String currentConsumption = filterSize(consumption.getConsumption());
                String quota = filterSize(consumption.getQuota());
                activity.runOnUiThread(() -> consumptionView.setText(currentConsumption.concat(" / ").concat(quota)));
            }

            @Override
            public void onGetError() {
                activity.runOnUiThread(() -> Toast.makeText(getActivity(), "Error retrieving consumption", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private String filterSize(long size) {

        if (size < 1024) return size + " Bytes";
        size /= 1024;

        if (size < 1024) return size + " Kb";
        size /= 1024;

        if (size < 1024) return size + " Mb";
        size /= 1024;

        if (size < 1024) return size + " Gb";
        size /= 1024;

        return size + " Tb";
    }
}
