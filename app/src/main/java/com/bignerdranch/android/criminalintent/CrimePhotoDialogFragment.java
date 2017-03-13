package com.bignerdranch.android.criminalintent;


import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;



/**
 * Created by andro1d on 07.03.2017.
 */

public class CrimePhotoDialogFragment extends DialogFragment {

    private static final String ARG_CRIME = "photo";

    public static CrimePhotoDialogFragment newInstance (String pathFile){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME,pathFile);

        CrimePhotoDialogFragment cpdf = new CrimePhotoDialogFragment();
        cpdf.setArguments(args);

        return cpdf;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String path = (String) getArguments().getSerializable(ARG_CRIME);

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_photo, null);
        ImageView imageView = (ImageView) v.findViewById(R.id.dialog_photo_image);
        imageView.setImageBitmap(BitmapFactory.decodeFile(path));
       // return super.onCreateDialog(savedInstanceState);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .create();
    }
}
