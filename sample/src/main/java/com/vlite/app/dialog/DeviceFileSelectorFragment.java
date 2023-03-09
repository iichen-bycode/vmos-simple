package com.vlite.app.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vlite.app.R;
import com.vlite.app.bean.VmFileItem;
import com.vlite.app.databinding.DialogFileSelectorBinding;

import java.util.List;

/**
 * 真机文件选择器
 */
public class DeviceFileSelectorFragment extends BaseBottomSheetDialogFragment {
    private DialogFileSelectorBinding binding;
    private OnFileSelectorListener fileSelectorListener;

    public static DeviceFileSelectorFragment newInstance(String title, String[] fileSuffixes, boolean supportUseDirectory) {
        return newInstance(title, fileSuffixes, supportUseDirectory, null);
    }

    public static DeviceFileSelectorFragment newInstance(String title, String[] fileSuffixes, boolean supportUseDirectory, String rootDir) {
        Bundle args = new Bundle();
        args.putString("title", title);
//        if (mimeTypes != null) args.putStringArray("disabled_mime_types", mimeTypes);
        if (fileSuffixes != null) args.putStringArray("disabled_file_suffixes", fileSuffixes);
        if (!TextUtils.isEmpty(rootDir)) args.putString("root_dir", rootDir);
        args.putBoolean("support_use_directory", supportUseDirectory);
        DeviceFileSelectorFragment fragment = new DeviceFileSelectorFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogFileSelectorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            binding.cbQueryProvider.setOnCheckedChangeListener((buttonView, isChecked) -> {
                final List<Fragment> fragments = getChildFragmentManager().getFragments();
                for (Fragment fragment : fragments) {
                    if (fragment instanceof FilesFragment) {
                        ((FilesFragment) fragment).setQueryProvider(isChecked);
                    }
                }
            });

            // fragment
            final DeviceFilesFragment fragment = DeviceFilesFragment.newInstance(getArguments());
            fragment.setFileItemListener(new FilesFragment.OnFileItemListener() {
                @Override
                public void onEnterDirectory(Fragment fragment, String path) {

                }

                @Override
                public void onClickFile(Fragment fragment, VmFileItem fileItem) {
                    try {
                        dismissAllowingStateLoss();
                        if (fileSelectorListener != null) {
                            fileSelectorListener.onFileSelected(fileItem);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            getChildFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void setBehaviorPeekHeight(Dialog dialog, int peekHeight) {
        super.setBehaviorPeekHeight(dialog, peekHeight);
        binding.getRoot().setMinHeight(peekHeight);
    }

    public void setOnFileSelectorListener(OnFileSelectorListener listener) {
        this.fileSelectorListener = listener;
    }

    public interface OnFileSelectorListener {
        void onFileSelected(VmFileItem item);
    }
}
