package com.linute.linute.MainContent;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import com.linute.linute.MainContent.SlidingTab.SlidingTabLayout;
import com.linute.linute.R;

/**
 * Created by QiFeng on 1/20/16.
 */
public class DiscoverHolderFragment extends Fragment {

    public DiscoverHolderFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discover_holder, container, false);

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.discover_sliding_tabs);

        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.discover_hostViewPager);

        viewPager.setAdapter(new FragmentHolderPagerAdapter(getChildFragmentManager()));

        tabLayout.setupWithViewPager(viewPager);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        MainActivity mainActivity = (MainActivity)getActivity();

        if (mainActivity!=null){
            mainActivity.setTitle("FEED");
            mainActivity.lowerAppBarElevation();
            mainActivity.showFAB(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        MainActivity mainActivity = (MainActivity)getActivity();
        if (mainActivity!=null){
            mainActivity.raiseAppBarLayoutElevation();
            mainActivity.showFAB(false);
        }
    }
}
