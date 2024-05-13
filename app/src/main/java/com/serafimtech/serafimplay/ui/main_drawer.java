package com.serafimtech.serafimplay.ui;

import static com.serafimtech.serafimplay.App.getApp;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.deleteRecursive;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.loadImageFromStorage;
import static com.serafimtech.serafimplay.file.value.InternalFileName.USER_AVATAR;
import static com.serafimtech.serafimplay.file.value.InternalFileName.USER_INFO;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.serafimtech.serafimplay.App;
import com.serafimtech.serafimplay.MainActivity;
import com.serafimtech.serafimplay.R;
import com.serafimtech.serafimplay.tool.KeepStateNavigator;
import com.serafimtech.serafimplay.tool.RoundImageView;
import com.serafimtech.serafimplay.ui.tool.DraggableFloatWindow;
import com.serafimtech.serafimplay.ui.tool.ProgressDialogUtil;

public class main_drawer extends Fragment implements NavigationView.OnNavigationItemSelectedListener {
    String TAG = main_drawer.class.getSimpleName();
    DrawerLayout drawer;
    NavigationView navigationView;
    ImageView memberBtn;
    RoundImageView userAvatar;
    //    SwitchCompat aSwitch;
    private FirebaseAuth mAuth;
    ProgressDialogUtil mProgressDialogUtil;
    DraggableFloatWindow alertFloatWindow;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.drawer_layout, container, false);

        navigationView = root.findViewById(R.id.nested);
        navigationView.setNavigationItemSelectedListener(this);

        drawer = root.findViewById(R.id.drawer);
        memberBtn = root.findViewById(R.id.mImgPhoto);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        memberBtn.setOnClickListener(v -> drawer.openDrawer(GravityCompat.END));

        ((MainActivity) requireActivity()).bindService();

//        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        ViewGroup.LayoutParams params = navigationView.getLayoutParams();
        params.width = getResources().getDisplayMetrics().widthPixels / 2; //屏幕的三分之一
        navigationView.setLayoutParams(params);

        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        NavHostFragment navHostFragment = (NavHostFragment) getChildFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController.getNavigatorProvider().addNavigator(new KeepStateNavigator(requireActivity(), navHostFragment.getChildFragmentManager(), R.id.nav_host_fragment));
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

        mProgressDialogUtil = new ProgressDialogUtil();
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.version).setTitle(getResources().getString(R.string.app_version) + ": " + getAppVersion(getContext()));
//        aSwitch = menu.findItem(R.id.sync).getActionView().findViewById(R.id.sync_switch);
//        aSwitch.setChecked(ReadFile(USER_INFO, DATA_SYNC).contains("true"));
//        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            buttonView.setClickable(false);
//            RequestPermissionManager rpManager = new RequestPermissionManager(getApp());
//            if (rpManager.isConnected()) {
//                WriteFile(String.valueOf(isChecked), USER_INFO, DATA_SYNC);
//                if (isChecked) {
//                    mProgressDialogUtil.showProgressDialogWithMessage(getActivity(), getResources().getString(R.string.setting));
//                    FTP.getInstance().jobToDo.add(new Object[]{
//                            FTP.JobAction.DownloadDir,
//                            new ArrayList<String>() {{
//                                add(CUSTOM_INFO);
//                                add(CUSTOM_INFO_BIND_GAME);
//                                add(CUSTOM_MACRO_INFO);
//                            }}
//                    });
//
//                    FTP.getInstance().jobToDo.add(new Object[]{
//                            FTP.JobAction.UploadDir,
//                            new ArrayList<String>() {{
//                                add(CUSTOM_INFO);
//                                add(CUSTOM_INFO_BIND_GAME);
//                                add(CUSTOM_MACRO_INFO);
//                            }}
//                    });
//
//                    FTP.getInstance().uploadActionToFTP(FTP.Action.Default, (currentStep) -> {
//                        if (currentStep.equals(FTP_UPLOAD_SUCCESS)) {
//                            mProgressDialogUtil.dismiss();
//                            buttonView.setClickable(true);
//                        }
//                    });
//
////                        alertFloatWindow = new DraggableFloatWindow(getActivity(), DraggableFloatView.LayoutType.AlertDialog_3);
////                        alertFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
////                            @Override
////                            public void onClick(View view) {
////                                switch (view.getTag().toString()) {
////                                    case "yes":
////                                        FTP.getInstance().uploadMultiFileToFTP(CUSTOM_INFO);
////                                        FTP.getInstance().uploadMultiFileToFTP(CUSTOM_INFO_BIND_GAME);
////                                        FTP.getInstance().uploadMultiFileToFTP(CUSTOM_MACRO_INFO, (currentStep, uploadSize, file) -> {
////                                            Log.d("FTP", currentStep);
////                                            if (currentStep.equals(FTP_UPLOAD_SUCCESS)) {
////                                                Log.d("FTP", "FTP upload successful");
////                                                FTP.getInstance().downloadFromFTP(CUSTOM_INFO);
////                                                FTP.getInstance().downloadFromFTP(CUSTOM_INFO_BIND_GAME);
////                                                FTP.getInstance().downloadFromFTP(CUSTOM_MACRO_INFO);
////                                            } else if (currentStep.equals(FTP_UPLOAD_LOADING)) {
////                                                long fize = file.length();
////                                                float num = (float) uploadSize / (float) fize;
////                                                int result = (int) (num * 100);
////                                                Log.d("FTP", "FTP upload progress:" + result + "%");
////                                            }
////                                        });
////                                        break;
////                                    case "no":
////                                        FTP.getInstance().downloadFromFTP(CUSTOM_INFO);
////                                        FTP.getInstance().downloadFromFTP(CUSTOM_INFO_BIND_GAME);
////                                        FTP.getInstance().downloadFromFTP(CUSTOM_MACRO_INFO, (currentStep, downProcess, file) -> {
////                                            Log.d("Download FTP", "Step " + currentStep);
////                                            if(currentStep.equals(FTP_FINISH) || currentStep.equals(FTP_FILE_NOTEXISTS)) {
////                                                FTP.getInstance().uploadMultiFileToFTP(CUSTOM_INFO);
////                                                FTP.getInstance().uploadMultiFileToFTP(CUSTOM_INFO_BIND_GAME);
////                                                FTP.getInstance().uploadMultiFileToFTP(CUSTOM_MACRO_INFO);
////                                            }
////                                        });
////                                        break;
////                                }
////                                alertFloatWindow.dismiss();
////                            }
////
////                            @Override
////                            public void onSet(View view) {
////
////                            }
////                        });
////                        alertFloatWindow.show();
//                } else {
//                    FTP.getInstance().jobToDo = new ArrayList<>();
//                    FTP.getInstance().uploadActionToFTP(FTP.Action.Remove, null);
//                    mProgressDialogUtil.dismiss();
//                    buttonView.setClickable(true);
//                }
//            }
//        });
        userAvatar = navigationView.getHeaderView(0).findViewById(R.id.user_avatar);
        try {
            if (loadImageFromStorage(USER_INFO, USER_AVATAR) != null) {
                userAvatar.setImageBitmap(loadImageFromStorage(USER_INFO, USER_AVATAR));
            } else {
                userAvatar.setImageResource(R.drawable.user);
            }
        } catch (Exception e) {
            userAvatar.setImageResource(R.drawable.user);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity) getActivity()).unbindService();
        ((MainActivity) getActivity()).closefloatwindow();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.choose:
                Navigation.findNavController(requireView()).navigateUp();
                ((MainActivity) requireActivity()).closefloatwindow();
                ((MainActivity) requireActivity()).unbindService();
                break;
            case R.id.login:
                signOut();
                Navigation.findNavController(requireView()).navigate(R.id.action_main_drawer_to_login);
                ((MainActivity) requireActivity()).closefloatwindow();
                ((MainActivity) requireActivity()).unbindService();
                break;
            case R.id.notification:
                Navigation.findNavController(requireView()).navigate(R.id.action_main_drawer_to_notification);
                break;
            case R.id.sync:
//                Log.d("menuiTEM",menuItem.isChecked()+"");
//                ((Switch)(menuItem.getActionView().findViewById(R.id.sync_switch))).setChecked(!menuItem.isChecked());
//                menuItem.setChecked(!menuItem.isChecked());
                break;
//            case R.id.share:
//                Navigation.findNavController(requireView()).navigate(R.id.action_main_drawer_to_share);
//                break;
        }
        return true;
    }

    void signOut() {
        deleteRecursive(getActivity().getDir(USER_INFO, Context.MODE_PRIVATE));
        // Firebase sign out

        if (mAuth.getCurrentUser() != null) {
            for (UserInfo userInfo : mAuth.getCurrentUser().getProviderData()) {
                Log.d("main_drawer", userInfo.getProviderId());
                if (userInfo.getProviderId().equals("facebook.com")) {
                    Log.d("main_drawer", "Sign out with facebook");
                    mAuth.signOut();
                    LoginManager.getInstance().logOut();
                }
                if (userInfo.getProviderId().equals("google.com")) {
                    Log.d("main_drawer", "Sign out with google");
                    mAuth.signOut();
                    // Google sign out
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build();
                    GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
                    mGoogleSignInClient.signOut().addOnCompleteListener(getActivity(),
                            (@NonNull Task<Void> task) -> {
                            });
                }
                if (userInfo.getProviderId().equals("password")) {
                    mAuth.signOut();
                }
            }
        } else {
            try {
            } catch (Exception e) {
                e.printStackTrace();
            }
            //wx sign out
        }
    }

    private String getAppVersion(Context context) {
        String result = "";
        try {
            result = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            result = result.replaceAll("[a-zA-Z] | =", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
