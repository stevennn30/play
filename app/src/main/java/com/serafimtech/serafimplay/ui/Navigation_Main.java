package com.serafimtech.serafimplay.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.serafimtech.serafimplay.App;
import com.serafimtech.serafimplay.R;
import com.serafimtech.serafimplay.tool.KeepStateNavigator;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Navigation_Main extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.navigation_main, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        NavHostFragment navHostFragment = (NavHostFragment) getChildFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController.getNavigatorProvider().addNavigator(new KeepStateNavigator(getActivity(), navHostFragment.getChildFragmentManager(), R.id.nav_host_fragment));
        switch (App.productName) {
            case R_Series:
                navController.setGraph(R.navigation.r);
                break;
            case S_Series:
                navController.setGraph(R.navigation.s);
                break;
            default:
                break;
        }
        BottomNavigationView navMenu = view.findViewById(R.id.nav_view);
        NavController navController2 = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(navMenu, navController2);
    }
}