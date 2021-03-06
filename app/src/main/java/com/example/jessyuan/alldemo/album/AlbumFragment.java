package com.example.jessyuan.alldemo.album;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.jessyuan.alldemo.R;
import com.example.jessyuan.alldemo.base.BaseNaviFragment;
import com.example.jessyuan.alldemo.camera.DefaultCameraModule;
import com.example.jessyuan.alldemo.camera.ImageCaptureReadyListener;
import com.example.jessyuan.alldemo.model.Folder;
import com.example.jessyuan.alldemo.model.Image;
import com.example.jessyuan.alldemo.ui.GridPlacingDecoration;
import com.example.mylibrary.ToastUtils;
import com.example.mylibrary.common.CommonRCLVAdapter;
import com.example.permissionmanager.PermissionListener;
import com.example.permissionmanager.PermissionManager;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

import static com.example.jessyuan.alldemo.camera.DefaultCameraModule.REQUEST_IMAGE_CAPTURE;

/**
 * Created by JessYuan on 19/10/2016.
 */

public class AlbumFragment extends BaseNaviFragment implements AlbumContract.AlbumView {

    private static final String TAG = "相册 Fragment";

    @BindView(R.id.rcv_album)
    RecyclerView albumRecyclerView;

    @Inject
    AlbumPresenter mPresenter;
    @Inject
    DefaultCameraModule mCameraModule;

    CommonRCLVAdapter<Folder> mFolderAdapter;
    CommonRCLVAdapter<Image> mImageAdapter;

    private GridPlacingDecoration mDecoration;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        DaggerAlbumComponent.builder()
                .albumModule(new AlbumModule(getActivity(),this)).build().inject(this);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setContentView(R.layout.fragment_pick_pictures);

        setupAdapter();
        PermissionManager.askPermission(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                "Grant Read external storage can load all images from your device",
                new PermissionListener() {
                    @Override
                    public void onResult(String permission, boolean permissionGranted) {
                        mPresenter.loadDeviceImages(false);
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_camera, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home && getToolbar().getTitle().equals("Album")) {
            getFragmentManager().popBackStack();
            removeMenuItem();
            return true;
        } else if (id == R.id.item_toolbar_camera) {
            PermissionManager.askPermission(getContext(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    "Grant Camera permission to Take a photo",
                    new PermissionListener() {
                        @Override
                        public void onResult(String permission, boolean permissionGranted) {
                            if (permission == Manifest.permission.CAMERA && permissionGranted) {
                                takePhoto();
                            }
                        }
                    }
            );
            return true;
        } else {
            mPresenter.loadDeviceImages(true);
            return true;
        }
    }

    /**
     * Take a photo
     */
    private void takePhoto() {
        Intent intent = mCameraModule.getCameraIntent(getActivity());
        if (intent != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE
                && resultCode == getActivity().RESULT_OK) {
            mCameraModule.getImage(getActivity(), data, new ImageCaptureReadyListener() {
                @Override
                public void onImageReady(Image image) {
                    mPresenter.loadDeviceImages(true);
                }
            });
        }
    }

    /**
     * Create FolderAdapter and ImageAdapter
     */
    private void setupAdapter() {
        mFolderAdapter = new CommonRCLVAdapter<Folder>(getContext(), R.layout.item_folder, null) {
            @Override
            public void onBindViewHolder(CommonViewHolder holder, int position, final Folder data) {
                holder.getTextViewById(R.id.tv_item_folder_name).setText(data.getFolderName());
                holder.getTextViewById(R.id.tv_item_image_number).setText(String.valueOf(data.getImages().size()));

                Glide.with(getActivity())
                        .load(data.getImages().get(0).getPath())
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder)
                        .into(holder.getImageViewById(R.id.iv_item_image));

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPresenter.openFolder(data);
                    }
                });
            }
        };

        mImageAdapter = new CommonRCLVAdapter<Image>(getContext(), R.layout.item_image, null) {
            @Override
            public void onBindViewHolder(CommonViewHolder holder, final int position, final Image data) {
                Glide.with(getActivity())
                        .load(data.getPath())
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder)
                        .into(holder.getImageViewById(R.id.iv_item_image));

                ImageView imageMask = holder.getImageViewById(R.id.iv_item_image_mask);
                ImageView selectedView = holder.getImageViewById(R.id.iv_item_image_selected);

                imageMask.setVisibility(mPresenter.isEdit() ? View.VISIBLE : View.GONE);
                selectedView.setVisibility((mPresenter.isEdit() && data.isSelected()) ?
                        View.VISIBLE : View.GONE);

                // short click
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mPresenter.isEdit()) {
                            mPresenter.selectImage(position);
                        } else {
                            mPresenter.openImageViewer(position);
                        }
                    }
                });

                // long click to image
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        addMenuItem();
                        mPresenter.setEdit(true);
                        mPresenter.selectImage(position);
                        return true;
                    }
                });
            }
        };
    }

    /**
     * Setup RecyclerView's layout manager, item decoration, and adapter
     */
    private void setRecyclerView(int column, CommonRCLVAdapter adapter) {
        if (mDecoration == null) {
            mDecoration = new GridPlacingDecoration(column,
                    getContext().getResources().getDimensionPixelSize(R.dimen.item_padding), false);
        } else {
            albumRecyclerView.removeItemDecoration(mDecoration);
            mDecoration = new GridPlacingDecoration(column,
                    getContext().getResources().getDimensionPixelSize(R.dimen.item_padding), false);
            albumRecyclerView.addItemDecoration(mDecoration);
        }

        albumRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), column));
        albumRecyclerView.setAdapter(adapter);
    }

    /**
     * Update toolbar title when change recyclerview adapter
     */
    private void updateToolbarTitle(String title) {
        getToolbar().setTitle(title);
    }

    private void addMenuItem() {
        getToolbar().getMenu().add(Menu.NONE, R.id.menu_item_delete, Menu.NONE, "delete").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        getToolbar().getMenu().add(Menu.NONE, R.id.menu_item_cancel, Menu.NONE, "cancel").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        getToolbar().getMenu().findItem(R.id.menu_item_delete).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                removeMenuItem();
                mPresenter.setEdit(false);
                mPresenter.deleteSelectedImage();
                return true;
            }
        });

        getToolbar().getMenu().findItem(R.id.menu_item_cancel).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ToastUtils.makeTextShort(getContext(), "cancel");
                removeMenuItem();
                mPresenter.setEdit(false);
                return true;
            }
        });
    }

    private void removeMenuItem() {
        getToolbar().getMenu().removeItem(R.id.menu_item_delete);
        getToolbar().getMenu().removeItem(R.id.menu_item_cancel);
    }

    @Override
    public void showTitle(String title) {
        getToolbar().setTitle(title);
    }

    @Override
    public void showFolders(List<Folder> list) {
        if (list != null) {
            mFolderAdapter.setData(list);
        }

        setRecyclerView(2, mFolderAdapter);
        updateToolbarTitle("Album");
    }

    @Override
    public void showImages(Folder folder, List<Image> list) {
        if (list != null) {
            mImageAdapter.setData(list);
        }

        setRecyclerView(3, mImageAdapter);
        updateToolbarTitle(folder.getFolderName());
    }

    @Override
    public void showImage(List<Image> list, int position) {
        ImageViewerFragment fragment = ImageViewerFragment.newInstance(list, position);
        fragment.show(getFragmentManager(), "slideshow");
    }

    @Override
    public void showChangeImage(int position) {
        if (position >= 0 && position < mImageAdapter.getItemCount()) {
            mImageAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void showImageEditOrNormalMode() {
        mImageAdapter.notifyDataSetChanged();
    }
}
