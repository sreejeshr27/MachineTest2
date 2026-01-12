package com.personal.machinetest.UI;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.personal.machinetest.R;
import com.personal.machinetest.Util.NetworkUtil;
import com.personal.machinetest.Util.TokenManager;
import com.personal.machinetest.databinding.ActivityLoginBinding;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private TokenManager tokenManager;
    private CredentialManager credentialManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tokenManager = new TokenManager(this);
        credentialManager = CredentialManager.create(this);
        binding.btSignIn.setOnClickListener(v -> {
            if (NetworkUtil.isNetworkAvailable(this)) {
                SignInProcess();
            } else {
                Toast.makeText(this, "Login requires internet connection", Toast.LENGTH_SHORT).show();
            }
        });

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAuth();
    }

    private void checkAuth() {
        String token = tokenManager.getToken();
        if (token != null) {
            if (NetworkUtil.isNetworkAvailable(this)) {
                navigateToMain();
            } else {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                tokenManager.clearToken();
            }
        }
    }

    private void SignInProcess() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(false).setServerClientId(getString(R.string.WebClientId)).setAutoSelectEnabled(true).build();
        GetCredentialRequest credentialRequest = new GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build();

        CancellationSignal cancellationSignal = new CancellationSignal();
        Executor executor = Executors.newSingleThreadExecutor();
        credentialManager.getCredentialAsync(this, credentialRequest, cancellationSignal, executor, new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
            @Override
            public void onResult(GetCredentialResponse getCredentialResponse) {
                handleSignIn(getCredentialResponse);
            }

            @Override
            public void onError(@NonNull GetCredentialException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("AUTH", "ERROR : ", e);
                    }
                });
            }
        });
    }

    private void handleSignIn(GetCredentialResponse credentialResponse) {
        if (credentialResponse.getCredential() instanceof CustomCredential) {
            CustomCredential credential = (CustomCredential) credentialResponse.getCredential();
            if (credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                try {
                    GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.getData());
                    String tokenId = googleIdTokenCredential.getIdToken();
                    tokenManager.saveToken(tokenId);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            navigateToMain();
                        }
                    });
                } catch (Exception e) {
                    Log.e("Auth Received", "Error", e);
                }
            }
        }
    }

    public void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

}