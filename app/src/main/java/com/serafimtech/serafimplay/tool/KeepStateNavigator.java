package com.serafimtech.serafimplay.tool;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigator;
import androidx.navigation.fragment.FragmentNavigator;

import com.serafimtech.serafimplay.R;
import com.serafimtech.serafimplay.ui.ViewModel;

import java.util.HashMap;

/**
 * @description
 * @author: Created jiangjiwei in 2019-07-23 15:05
 */
@Navigator.Name("keep_state_fragment")
public class KeepStateNavigator extends FragmentNavigator {
    private Context context;
    private FragmentManager manager;
    private int containerId;

    public KeepStateNavigator(@NonNull Context context, @NonNull FragmentManager manager, int containerId) {
        super(context, manager, containerId);
        this.context = context;
        this.manager = manager;
        this.containerId = containerId;
    }

    @Nullable
    @Override
    public NavDestination navigate(@NonNull Destination destination, @Nullable Bundle args, @Nullable NavOptions navOptions, @Nullable Navigator.Extras navigatorExtras) {
//        super.navigate(destination,args,navOptions,navigatorExtras);
        String tag = String.valueOf(destination.getId());
        FragmentTransaction transaction = manager.beginTransaction();
        boolean initialNavigate = false;
        Fragment currentFragment = manager.getPrimaryNavigationFragment();

        if (currentFragment != null) {
//            transaction.detach(currentFragment);
            transaction.hide(currentFragment);
        } else {
            initialNavigate = true;
        }

        Fragment fragment = manager.findFragmentByTag(tag);
        String className = destination.getClassName();
        String last = className.substring(className.lastIndexOf('.') + 1);

        ViewModel model = new ViewModelProvider((ViewModelStoreOwner) context).get(ViewModel.class);
        MutableLiveData<String> mutableLiveData = (MutableLiveData<String>) model.getnavigationpage();
//        Log.d("AAAA", navigation_page_num(mutableLiveData.getValue()) + "," + navigation_page_num(last));
        if (navigation_page_num(mutableLiveData.getValue()) > navigation_page_num(last)) {
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        } else {
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        }


        if(last.contains("Gamepage")) {
            if (mutableLiveData.getValue().contains("Home")) {
                mutableLiveData.setValue(last + "_H");
            } else if (mutableLiveData.getValue().contains("FeaturedGame")) {
                mutableLiveData.setValue(last + "_F");
            }
        } else {
            Log.e("last",""+last);
            mutableLiveData.setValue(last);
        }

        if (fragment == null) {
            fragment = manager.getFragmentFactory().instantiate(context.getClassLoader(), className);
            transaction.add(containerId, fragment, tag);
        } else {
//            transaction.attach(fragment);
            transaction.show(fragment);
        }

        transaction.setPrimaryNavigationFragment(fragment);
        transaction.setReorderingAllowed(true);
        transaction.commitNow();
        return initialNavigate ? destination : null;
    }

    private int navigation_page_num(String classname) {
        int num = 0;
        switch (classname) {
            case "Navigation_SHome":
            case "Navigation_RHome":
                num = 0;
                break;
            case "Navigation_Gamepage_H":
            case "Navigation_Gamelist":
                num = 1;
                break;
            case "Navigation_FeaturedGame":
                num = 2;
                break;
            case "Navigation_Gamepage_F":
                num = 3;
                break;
            case "Navigation_Ble":
                num = 4;
                break;
            case "Navigation_Operation_S":
            case "Navigation_Operation_R":
                num = 5;
                break;
            case "Navigation_Mall":
                num = 6;
                break;
            case "Navigation_Gamepage":
                num = 7;
                break;
        }
        return num;
    }
}

