package com.linute.linute.MainContent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.linute.linute.R;

/**
 * Created by QiFeng on 11/17/15.
 */
public class DiscoverFragment extends Fragment {


    private RecyclerView recList;
    private LinearLayoutManager llm;
    private MyAdapter mAdapterMyAdapter;
    private MyAdapter mAdapter;

    //called when fragment drawn the first time
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discover,container,false); //setContent

        recList = (RecyclerView) rootView.findViewById(R.id.eventList);
        recList.setHasFixedSize(true);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        String [] myDataset = {"Hi", "Cool EVen", "Wow"};

        mAdapter = new MyAdapter(myDataset);
        recList.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

       // Button but = (Button) getActivity().findViewById(R.id.button);
        /*
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
            }

        });

        Button but2 = (Button)getActivity().findViewById(R.id.button2);
        but2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AppCompatActivity)getActivity()).getSupportActionBar().show();
            }
        });*/

    }
}
