package com.feeds.feedposts.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.feeds.feedposts.R;
import com.feeds.feedposts.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout layoutRoot;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference users;
    public static String loggedInUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFirebase();
        initViews();
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        users = firebaseDatabase.getReference(getString(R.string.Users));
    }

    private void initViews() {
        Button btnSignIn = findViewById(R.id.btnSignIn);
        Button btnRegister = findViewById(R.id.btnRegister);
        layoutRoot = findViewById(R.id.rootLayout);
        btnSignIn.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSignIn:
                showLoginDialog();
                break;

            case R.id.btnRegister:
                showRegistrationDialog();
                break;
        }
    }


    private void showRegistrationDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.register_dia_msg));
        dialog.setMessage(getString(R.string.please_use_email));

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.layout_register, null);

        final EditText editEmail = register_layout.findViewById(R.id.editEmail);
        final EditText editPassword = register_layout.findViewById(R.id.editPassword);
        final EditText editName = register_layout.findViewById(R.id.editName);

        dialog.setView(register_layout);

        // set button
        dialog.setPositiveButton(getString(R.string.register_dia_msg), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Check validation

                if (TextUtils.isEmpty(editEmail.getText().toString())) {
                    Snackbar.make(layoutRoot, getString(R.string.please_enter_email), Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(editPassword.getText().toString())) {
                    Snackbar.make(layoutRoot, getString(R.string.please_enter_password), Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (editPassword.getText().toString().length() < 6) {
                    Snackbar.make(layoutRoot, getString(R.string.password_short), Snackbar.LENGTH_SHORT).show();
                    return;
                }

                dialog.dismiss();

                final SpotsDialog waitingDialog = new SpotsDialog(MainActivity.this, getString(R.string.registering));
                waitingDialog.show();

                firebaseAuth.createUserWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                User user = new User();
                                user.setEmail(editEmail.getText().toString());
                                user.setName(editName.getText().toString());
                                user.setPassword(editPassword.getText().toString());

                                // Use Email to Key
                                if (FirebaseAuth.getInstance().getCurrentUser() != null)
                                    users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .setValue(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    waitingDialog.dismiss();
                                                    Snackbar.make(layoutRoot, getString(R.string.register_success), Snackbar.LENGTH_SHORT).show();

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            waitingDialog.dismiss();
                                            Snackbar.make(layoutRoot, getString(R.string.registration_failed) + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                        }
                                    });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();
                        Snackbar.make(layoutRoot, getString(R.string.registration_failed) + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });

            }
        });


        dialog.setNegativeButton(getString(R.string.cancel_dia_msg), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }


    private void showLoginDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.sign_in_caps));
        dialog.setMessage(getString(R.string.please_use_email_sign_in));

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_login, null);

        final EditText editEmail = login_layout.findViewById(R.id.editEmail);
        final EditText editPassword = login_layout.findViewById(R.id.editPassword);

        dialog.setView(login_layout);

        // set button
        dialog.setPositiveButton(getString(R.string.sign_in_caps), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //btnSignIn.setEnabled(false);

                // Check validation
                if (TextUtils.isEmpty(editEmail.getText().toString())) {
                    Snackbar.make(layoutRoot, getString(R.string.please_enter_email), Snackbar.LENGTH_SHORT).show();
                    return;
                }


                if (TextUtils.isEmpty(editPassword.getText().toString())) {
                    Snackbar.make(layoutRoot, getString(R.string.please_enter_password), Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (editPassword.getText().toString().length() < 6) {
                    Snackbar.make(layoutRoot, getString(R.string.password_short), Snackbar.LENGTH_SHORT).show();
                    return;
                }

                dialog.dismiss();

                final SpotsDialog waitingDialog = new SpotsDialog(MainActivity.this, R.string.authenticating);
                waitingDialog.show();

                // Login
                firebaseAuth.signInWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                if (FirebaseAuth.getInstance().getCurrentUser() != null)
                                    loggedInUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                waitingDialog.dismiss();
                                startActivity(new Intent(MainActivity.this, PostActivity.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();
                        Snackbar.make(layoutRoot, "Login Failed : " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });

            }
        });


        dialog.setNegativeButton(getString(R.string.cancel_dia_msg), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

}
