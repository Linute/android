package com.linute.linute.MainContent.DiscoverFragment;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;

/**
 * Created by QiFeng on 1/20/16.
 */
public class DiscoverHolderFragment extends UpdatableFragment {

    public static final String TAG = DiscoverHolderFragment.class.getSimpleName();

    private ViewPager mViewPager;

    private FragmentHolderPagerAdapter mFragmentHolderPagerAdapter;

    public DiscoverHolderFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentHolderPagerAdapter = new FragmentHolderPagerAdapter(getChildFragmentManager());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discover_holder, container, false);

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.discover_sliding_tabs);

        mViewPager = (ViewPager) rootView.findViewById(R.id.discover_hostViewPager);
        mViewPager.setAdapter(mFragmentHolderPagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        MainActivity mainActivity = (MainActivity)getActivity();

        if (mainActivity!=null){
            mainActivity.setTitle("FEED");
            mainActivity.lowerAppBarElevation(); //app bars elevation must be 0 or there will be a shadow on top of the tabs
            mainActivity.showFAB(true); //show the floating button
            mainActivity.resetToolbar();
        }

//        //this fragment will need updating if user posted something
//        //set current item to Campus discover fragment
//        //Discover fragment will do the rest
//        if (fragmentNeedsUpdating()){
//            mViewPager.setCurrentItem(0, true);
//        }
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
