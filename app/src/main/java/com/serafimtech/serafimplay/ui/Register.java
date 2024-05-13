package com.serafimtech.serafimplay.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.serafimtech.serafimplay.R;
import com.serafimtech.serafimplay.tool.RequestPermissionManager;

import org.jetbrains.annotations.NotNull;

public class Register extends Fragment {
    //<editor-fold desc="<Variable>">
    private EditText registrationUserName, registrationUserPassword, registrationReconfirmPassword;
    private Button signUpBtn;
    private ImageView returnBtn;
    private FirebaseAuth mAuth;
    private RequestPermissionManager rpManager;
    //</editor-fold>

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.register, container, false);
        rpManager = new RequestPermissionManager(getActivity());
        mAuth = FirebaseAuth.getInstance();
        registrationUserName = root.findViewById(R.id.registration_username);
        registrationUserPassword = root.findViewById(R.id.registration_password);
        registrationReconfirmPassword = root.findViewById(R.id.confirm_password);
        signUpBtn = root.findViewById(R.id.register);
        returnBtn = root.findViewById(R.id.return_btn);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });
        returnBtn.setOnClickListener((View v) -> {
            Navigation.findNavController(requireView()).navigateUp();
        });
    }

    private void registerNewUser() {

        String email, password, reconfirmPassword;
        email = registrationUserName.getText().toString();
        password = registrationUserPassword.getText().toString();
        reconfirmPassword = registrationReconfirmPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getActivity(), R.string.no_email_input, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getActivity(), R.string.no_password_input, Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(getActivity(), R.string.password_length, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(reconfirmPassword)) {
            Toast.makeText(getActivity(), R.string.password_reconfirm, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!reconfirmPassword.equals(password)) {
            Toast.makeText(getActivity(), R.string.reconfirm_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!rpManager.isConnected()) {
            Toast.makeText(getActivity(), R.string.no_internet_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), R.string.registration_successful, Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            user.sendEmailVerification().addOnCompleteListener(getActivity(), new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    // Re-enable button
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), getResources().getString(R.string.verification_send) + user.getEmail(), Toast.LENGTH_SHORT).show();
                                        Navigation.findNavController(requireView()).navigateUp();
                                    } else {
                                        Toast.makeText(getActivity(), R.string.verification_send_fail, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getActivity(), R.string.registration_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
