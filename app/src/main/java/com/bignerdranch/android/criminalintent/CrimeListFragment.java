package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;


/**
 * Created by andro1d on 21.01.2017.
 */

public class CrimeListFragment extends Fragment {

    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private boolean mSubtitleVisible;
    private Button mButtonAddCrime;
    private TextView mTVempty;
    private Callbacks mCallbacks;

    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    public static final int REQUEST_CRIME_ID = 0;

    public interface Callbacks {
        void onCrimeSelected(Crime crime);
    }


    @Override
    public void onAttach(Context activity){
        super.onAttach(activity);
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_crime_list,container, false);
        mCrimeRecyclerView = (RecyclerView) view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (savedInstanceState != null ){
            mSubtitleVisible =savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }
        //Упражнение 3 глава 13
        mTVempty = (TextView) view.findViewById(R.id.tv_if_recyc_view_empty);
        mButtonAddCrime = (Button) view.findViewById(R.id.add_crime);
        mButtonAddCrime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
//                Intent intent = CrimePagerActivity.newIntent(getActivity(),crime.getId());
//                startActivity(intent);
                updateUI();
                mCallbacks.onCrimeSelected(crime);
            }
        });


        updateUI();


        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }


    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.menu_item_show_subtitle);
        if (mSubtitleVisible){
            subtitleItem.setTitle(R.string.show_subtitle);
        } else{
            subtitleItem.setTitle(R.string.hide_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
//                Intent intent = CrimePagerActivity.newIntent(getActivity(),crime.getId());
//                startActivity(intent);
                updateUI();
                mCallbacks.onCrimeSelected(crime);
                return true;
            case R.id.menu_item_show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();

                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public void updateUI(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if (mAdapter == null){
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setCrimes(crimes); //обновляем список Crimes т.к. в нем могло изменится кличество элементов
            mAdapter.notifyDataSetChanged();
            //mAdapter.notifyItemChanged(crimeIdUpdated); //Упражнение из 10ой главы не работает - почему то передаётся не правильная позиция
            //и вообще не правильно тут писать ID. Надо писать position! Всё переделать!
        }
        updateSubtitle();

        //упражнение 3, глава 13
        if (mButtonAddCrime!=null) {
            if (crimes.size() > 0) {
                mButtonAddCrime.setVisibility(View.GONE);
                mTVempty.setVisibility(View.GONE);
            } else {
                mButtonAddCrime.setVisibility(View.VISIBLE);
                mTVempty.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateSubtitle(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();

        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, crimeCount, crimeCount);

        if (!mSubtitleVisible){
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
        activity.getSupportActionBar().getSubtitle();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK){
            return;
        }
        if (requestCode == REQUEST_CRIME_ID){
            if (data == null){
                return;
            }
            updateUI();
        }
    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        //public TextView mTitleTextView;
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private CheckBox mSolvedCheckBox;

        private Crime mCrime;

        public CrimeHolder(View itemView) {
            super(itemView);
            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_crime_title_text_view);
            mDateTextView = (TextView) itemView.findViewById(R.id.list_item_crime_date_text_view);
            mSolvedCheckBox = (CheckBox) itemView.findViewById(R.id.list_item_crime_solved_check_box);

            itemView.setOnClickListener(this);

        }

        public void bindCrime(Crime crime){
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(mCrime.getDate().toString());
            mSolvedCheckBox.setChecked(mCrime.isSolved());
        }

        @Override
        public void onClick(View v) {
//            Toast.makeText(getActivity(),
//                    mCrime.getTitle() + " cheked", Toast.LENGTH_SHORT).show();
            //Intent intent = new Intent(getActivity(), CrimeActivity.class);
            //Intent intent = CrimeActivity.newIntent(getActivity(), mCrime.getId());
         //   Intent intent = CrimePagerActivity.newIntent(getActivity(),mCrime.getId());
          //  startActivity(intent);
         //   startActivityForResult(intent, REQUEST_CRIME_ID);
            mCallbacks.onCrimeSelected(mCrime);

        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder>{

        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes){
            mCrimes = crimes;
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_crime, parent, false);

            return new CrimeHolder(view);
        }

        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bindCrime(crime);
        }

        @Override
        public int getItemCount() {

            return mCrimes.size();
        }

        //нужно для упражнения в 13ой главе - из-за того что меняется число элементов в списке crime, нужно его обновить в адаптере
        public void setCrimes(List<Crime> crimes){
            mCrimes = crimes;
        }

    }

}
