package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.style.ImageSpan;
import android.text.style.SubscriptSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.Date;
import java.util.UUID;

/**
 * Created by andro1d on 16.01.2017.
 */

public class CrimeFragment extends Fragment {



    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mSuspectCallButton;

    private Uri contactUri; //для 15 главы, упраждение

    private ImageButton mPhootoButton;
    private ImageView  mPhotoView;
    private Callbacks mCallbacks;

    private File mPhotoFile;


    public static final String ARG_CRIME_ID = "crime_id";
    public static final String EXTRA_GIVE_CRIME_ID =
            "com.bignerdranch.android.criminalintent.crime_for_crimelist";
    public static final String DIALOG_DATE = "DialogDate";
    public static final String DIALOG_TIME = "DialogTime";
    public static final String DIALOG_PHOTO = "DialogPhoto";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 22;
    private static final int REQUEST_PHOTO = 2;


    public interface Callbacks {
        void onCrimeUpdate(Crime crime);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public static CrimeFragment newInstance(UUID crimeId){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
        setHasOptionsMenu(true);

        //Передача CrimeListFragment crimeId того, которого нужно обновить, чтобы не обновлять весь список
        //получаем номер позиции в списке crimes
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int positionCrime = crimeLab.getPositionInCrimes(mCrime);

        Intent data = new Intent();
        data.putExtra(EXTRA_GIVE_CRIME_ID, positionCrime);
        getActivity().setResult(Activity.RESULT_OK,data);
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete_current_crime:
                //остановился тут - если оставить удаление, то глючит рецайкл вью. Надо разбираться
                CrimeLab cl = CrimeLab.get(getActivity());
                cl.deleteCrime(mCrime);
                getActivity().finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = (EditText)v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                updateCrime();
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mDateButton = (Button)v.findViewById(R.id.crime_date);
        updateDate();

        mDateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                FragmentManager manager = getActivity().getSupportFragmentManager() ;
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mTimeButton = (Button) v.findViewById(R.id.crime_time);
        updateTime();
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                TimePickerFragment timePickerFragment = TimePickerFragment.newInstance(mCrime.getDate());
                timePickerFragment.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                timePickerFragment.show(manager,DIALOG_TIME);
            }
        });


        mSolvedCheckBox = (CheckBox)v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                updateCrime();
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Intent i = new Intent(Intent.ACTION_SEND);
                //Упражнение 15 главы. Интенты можно создавать через интентБуилдер. Работает так же как и строка выше
                Intent i = ShareCompat.IntentBuilder.from(getActivity()).getIntent();
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());

                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });
        if (mCrime.getSuspect() !=null){
            mSuspectButton.setText(mCrime.getSuspect());
        }

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) ==null){
            mSuspectButton.setEnabled(false);
        }


        mPhootoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);

        final Intent captureImage = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);

        boolean canTakePhoto = mPhotoFile !=null && captureImage.resolveActivity(packageManager) != null;
        mPhootoButton.setEnabled(canTakePhoto);

        if (canTakePhoto) {
            Uri uri = Uri.fromFile(mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }

        mPhootoButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 startActivityForResult(captureImage, REQUEST_PHOTO);
             }
        });

        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CrimePhotoDialogFragment crimePhotoDialogFragment = CrimePhotoDialogFragment.newInstance(mPhotoFile.getPath());
                FragmentManager manager = getActivity().getSupportFragmentManager();


                crimePhotoDialogFragment.show(manager, DIALOG_PHOTO);
            }
        });
                //15 глава. Упражнение - звонок подозреваемому.
                mSuspectCallButton = (Button) v.findViewById(R.id.crime_suspect_dial);
        mSuspectCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCrime.getSuspect() == null){
                    Toast.makeText(getContext(),R.string.crime_suspect_empty, Toast.LENGTH_SHORT).show();
                    return;
                }

                //Получение контакта по имени - не правильно (т.к. могут быть 2 контакта с одним именем), но через БД делать не стал т.к. нарушит схему БД
                //заполняем условие where
                String[] queryFields = new String[]{
                        ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID};
                //получаем Курсор с контактами
                Cursor c = getActivity().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,queryFields,null,null,null);

                String contactID = null;
                try {
                    if (c.getCount() ==0){
                        return;
                    }
                    //Ищем нужный нам контакт по имени
                    c.moveToFirst();
                    while (c.moveToNext()){
                        if (c.getString(0).equals(mCrime.getSuspect())){
                            //запоминаем ID контакта
                            contactID = c.getString(1);
                            break;
                        }
                    }
                } finally {
                    c.close();
                }

                //Получение номера телефона контакта по ID:
                String[] queryFieldsForPhone = new String[]{
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                };
                Cursor curPhone = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        queryFieldsForPhone,
                        null,
                        null,
                        null);

                String suspectNumber = null;
                while (curPhone.moveToNext()){
                   if (curPhone.getString(0).equals(contactID)){
                       mSuspectCallButton.setText(curPhone.getString(1));
                       suspectNumber = curPhone.getString(1);
                       break;
                   }
                }
                curPhone.close();
                Uri number = Uri.parse("tel:" + suspectNumber);
                Intent intent = new Intent(Intent.ACTION_DIAL, number);
                startActivity(intent);
            }
        });

        updatePhotoView();

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == REQUEST_DATE){
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateCrime();
            updateDate();
        }
        if (requestCode == REQUEST_TIME){
            Date date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mCrime.setDate(date);
            updateCrime();
            updateTime();
        } else if (requestCode == REQUEST_CONTACT && data != null){
            Uri contactUri = data.getData();
            this.contactUri = contactUri;
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID};
            Cursor c = getActivity().getContentResolver().query(contactUri,queryFields,null,null,null);

            try {
                if (c.getCount() ==0){
                    return;
                }
                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                updateCrime();
                mSuspectButton.setText(suspect);
                //c.moveToNext();
             //   contactID = c.getString(1);

            } finally {
                c.close();
                }
            }
        else if (requestCode == REQUEST_PHOTO){
            updateCrime();
            updatePhotoView();
        }
    }

    private void updateCrime(){
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdate(mCrime);
    }

    private void updateTime() {
        mTimeButton.setText(mCrime.getDate().toString());
    }

    private void updateDate() {

        mDateButton.setText(mCrime.getDate().toString());
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }
        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();
        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }
        String report = getString(R.string.crime_report, mCrime.getTitle(),
                dateString, solvedString, suspect);
        return report;
    }


    private void updatePhotoView() {

        if (mPhotoFile == null || !mPhotoFile.exists()){
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }
}
