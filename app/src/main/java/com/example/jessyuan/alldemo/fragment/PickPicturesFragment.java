package com.example.jessyuan.alldemo.fragment;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.jessyuan.alldemo.R;
import com.example.mylibrary.BitmapUtils;
import com.example.mylibrary.UriUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Jess Yuan on 19/10/2016.
 */

public class PickPicturesFragment extends BaseNaviFragment {

    private static final int REQUEST_PICK_PICTURES = 1;

    @BindView(R.id.iv_image)
    ImageView mImageView;

    @Override
    void setToolbar() {
        getToolbar().setTitle("Pick Pictures");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pick_pictures, container, false);
    }

    @OnClick(R.id.btn_pick_pictures_from_gallery)
    void pickpictures() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_PICTURES);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_PICTURES && resultCode == getActivity().RESULT_OK
                && data != null) {
            Uri uri = data.getData();

            String image = UriUtils.parseToPath(getActivity(), uri);

            mImageView.setImageBitmap(BitmapUtils.scale(mImageView, image));
        }
    }
}
