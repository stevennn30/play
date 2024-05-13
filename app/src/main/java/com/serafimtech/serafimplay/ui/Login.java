package com.serafimtech.serafimplay.ui;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.serafimtech.serafimplay.App.getApp;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.ReadFile;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.WriteFile;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.saveBitmapToInternalStorage;
import static com.serafimtech.serafimplay.file.value.InternalFileName.APP_INFO;
import static com.serafimtech.serafimplay.file.value.InternalFileName.APP_MODE;
import static com.serafimtech.serafimplay.file.value.InternalFileName.LOGIN_TYPE;
import static com.serafimtech.serafimplay.file.value.InternalFileName.USER_AVATAR;
import static com.serafimtech.serafimplay.file.value.InternalFileName.USER_EMAIL;
import static com.serafimtech.serafimplay.file.value.InternalFileName.USER_ID;
import static com.serafimtech.serafimplay.file.value.InternalFileName.USER_INFO;
import static com.serafimtech.serafimplay.file.value.InternalFileName.USER_NAME;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.serafimtech.serafimplay.R;
import com.serafimtech.serafimplay.file.Downloader;
import com.serafimtech.serafimplay.tool.RequestPermissionManager;
import com.serafimtech.serafimplay.ui.tool.ProgressDialogUtil;

import java.io.File;

public class Login extends Fragment {
    private String photoUrl;
    Button SignInBtn;
    private Button registerBtn;
    ImageView googleSignInBtn;
//    ImageView wxSignInBtn;
    ImageView testBtn;
    ImageView titleBtn;
    View layout;

    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private ProgressDialogUtil mSignInProgressDialog;
    public RequestPermissionManager rpManager;

    private static final int RC_SIGN_IN = 7;

    private String email;
    private String name;
    private EditText userAccount, userPassword;

    int count;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.login, container, false);

        SignInBtn = root.findViewById(R.id.log_in_btn);
        googleSignInBtn = root.findViewById(R.id.google_sign_in_btn);
//        wxSignInBtn = root.findViewById(R.id.wx_sign_in_btn);
        layout = root.findViewById(R.id.sign_in_layout);
        userAccount = root.findViewById(R.id.email_log_in_username);
        userPassword = root.findViewById(R.id.email_log_in_password);
        googleSignInBtn = root.findViewById(R.id.google_sign_in_btn);
//        wxSignInBtn = root.findViewById(R.id.wx_sign_in_btn);
        SignInBtn = root.findViewById(R.id.log_in_btn);
        registerBtn = root.findViewById(R.id.register_btn);
        titleBtn = root.findViewById(R.id.main_title);
        testBtn = root.findViewById(R.id.test);

        mSignInProgressDialog = new ProgressDialogUtil();
        mAuth = FirebaseAuth.getInstance();
        rpManager = new RequestPermissionManager(getActivity());

        return root;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        count = 0;
        getApp().betaFlag = ReadFile(APP_INFO, APP_MODE).contains("beta");
        if (getApp().betaFlag) {
            testBtn.setVisibility(View.VISIBLE);
        }
        titleBtn.setOnTouchListener((v, event) -> {
            count++;
            if (count == 7) {
                count = 0;
                if (getApp().betaFlag) {
                    getApp().betaFlag = false;
                    testBtn.setVisibility(View.GONE);
                    WriteFile("", APP_INFO, APP_MODE);
                } else {
                    testBtn.setVisibility(View.VISIBLE);
                    getApp().betaFlag = true;
                    WriteFile("beta", APP_INFO, APP_MODE);
                }
            }
            return false;
        });
        layout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (userAccount.isFocused()) {
                    Rect outRect = new Rect();
                    userAccount.getGlobalVisibleRect(outRect);
                    if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        userAccount.clearFocus();
                        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        try {
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (userPassword.isFocused()) {
                    Rect outRect = new Rect();
                    userPassword.getGlobalVisibleRect(outRect);
                    if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        userPassword.clearFocus();
                        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        try {
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return false;
        });
        testBtn.setOnClickListener((View v) -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_login_to_choose);
        });
        registerBtn.setOnClickListener((View v) -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_login_to_register);
        });
        userPassword.setOnEditorActionListener((v, actionId, event) -> {
            switch (actionId) {
                case EditorInfo.IME_ACTION_DONE:
                case EditorInfo.IME_ACTION_NEXT:
                case EditorInfo.IME_ACTION_PREVIOUS:
                    userPassword.clearFocus();
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    try {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
            }
            return false;
        });

        initializeEmailLogin();
        initializeGoogleLogin();
        initializeWXLogin();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            mSignInProgressDialog.showProgressDialogWithMessage(getActivity(), getResources().getString(R.string.connecting));
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
                Log.e("onActivityResult","RC_SIGN_IN");

            } catch (ApiException e) {
                Log.e("onActivityResult","RC_SIGN_IN_ERROR");

                mSignInProgressDialog.dismiss();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        File directory = getActivity().getDir("USER_INFO", Context.MODE_PRIVATE);
        File f = new File(directory, LOGIN_TYPE);
        if (f.exists()) {
            Navigation.findNavController(requireView()).navigate(R.id.action_login_to_choose);
        }
    }

    //<editor-fold desc="<SignIn>">
    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(getActivity(), R.string.google_login_successful, Toast.LENGTH_SHORT).show();
                        user = mAuth.getCurrentUser();
                        name = user.getDisplayName();
                        email = user.getEmail();
//                        Toast.makeText(getApplicationContext(), user.getUid(), Toast.LENGTH_LONG).show();
                        WriteFile("2", USER_INFO, LOGIN_TYPE);
                        WriteFile(user.getUid(), USER_INFO, USER_ID);
                        updateUI();
                    } else {
                        // If sign in fails, display a message to the user.
                        mSignInProgressDialog.dismiss();
                        Toast.makeText(getActivity(), R.string.google_login_failed, Toast.LENGTH_SHORT).show();
                    }

                    // ...
                });
    }

    private void firebaseAuthWithEmailAndPassword() {
        String account, password;
        account = userAccount.getText().toString();
        password = userPassword.getText().toString();

        if (TextUtils.isEmpty(account)) {
            Toast.makeText(getApplicationContext(), R.string.no_email_input, Toast.LENGTH_SHORT).show();
            mSignInProgressDialog.dismiss();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), R.string.no_password_input, Toast.LENGTH_SHORT).show();
            mSignInProgressDialog.dismiss();
            return;
        }
        if (!rpManager.isConnected()) {
            Toast.makeText(getActivity(), R.string.no_internet_connected, Toast.LENGTH_SHORT).show();
            mSignInProgressDialog.dismiss();
            return;
        }

        mAuth.signInWithEmailAndPassword(account, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!mAuth.getCurrentUser().isEmailVerified()) {
                            Toast.makeText(getActivity(), R.string.please_verify_email, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.email_login_successful, Toast.LENGTH_SHORT).show();
                            user = mAuth.getCurrentUser();
                            name = user.getDisplayName();
                            email = user.getEmail();
                            WriteFile("4", USER_INFO, LOGIN_TYPE);
                            WriteFile(email, USER_INFO, USER_EMAIL);
                            WriteFile(name, USER_INFO, USER_NAME);
                            WriteFile(user.getUid(), USER_INFO, USER_ID);

                            Navigation.findNavController(requireView()).navigate(R.id.action_login_to_choose);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.email_login_failed, Toast.LENGTH_SHORT).show();
                    }
                    mSignInProgressDialog.dismiss();
                });
    }

    private void initializeEmailLogin() {
        SignInBtn.setOnClickListener((View v) -> {
            mSignInProgressDialog.showProgressDialogWithMessage(getActivity(), getResources().getString(R.string.connecting));
            firebaseAuthWithEmailAndPassword();
        });
    }

    private void initializeGoogleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        googleSignInBtn.setOnClickListener((View v) -> {
            if (rpManager.isConnected()) {
                googleSignIn();
            } else {
                Toast.makeText(getActivity(), R.string.no_internet_connected, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initializeWXLogin() {
        // TODO: 2019/9/5 記得去改掉微信開發者平台的MD5認證碼，目前用的是debug的key
        /*wxSignInBtn.setOnClickListener((View v) -> {
            // send oauth request
            if (getApp().api.isWXAppInstalled()) {
                if (rpManager.isConnected()) {
                    SendAuth.Req req = new SendAuth.Req();
                    req.scope = "snsapi_userinfo";
                    req.state = "wechat_sdk_微信登录";
                    getApp().api.sendReq(req);
                } else {
                    Toast.makeText(getActivity(), R.string.no_internet_connected, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), R.string.wx_not_installed, Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    void updateUI() {
        new Thread(() -> {
            user = mAuth.getCurrentUser();
            try {
                photoUrl = user.getPhotoUrl().toString();
                for (UserInfo profile : user.getProviderData()) {
                    // check if the provider id matches "facebook.com"
                    if (profile.getProviderId().equals("facebook.com")) {
                        String facebookUserId = profile.getUid();
                        // construct the URL to the profile picture, with a custom height
                        // alternatively, use '?type=small|medium|large' instead of ?height=
                        photoUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?height=1080";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            WriteFile(email, USER_INFO, USER_EMAIL);
            WriteFile(name, USER_INFO, USER_NAME);
            WriteFile(user.getUid(), USER_INFO, USER_ID);
            Log.e("updateUI", "login");
            final Downloader.BasicImageDownloader downloader = new Downloader.BasicImageDownloader(new Downloader.BasicImageDownloader.OnImageLoaderListener() {
                @Override
                public void onError(Downloader.BasicImageDownloader.ImageError error) {
                    error.printStackTrace();
                }

                @Override
                public void onProgressChange(int percent) {
                }

                @Override
                public void onComplete(Bitmap result) {
                    saveBitmapToInternalStorage(result, USER_INFO, USER_AVATAR);
                    mSignInProgressDialog.dismiss();
                    Navigation.findNavController(requireView()).navigate(R.id.action_login_to_choose);
                }
            });
            downloader.download(photoUrl, true);
        }).start();
    }
    //</editor-fold>
}
