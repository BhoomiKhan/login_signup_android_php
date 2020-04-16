package com.coriumvpn.loginsignup;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupScreen extends AppCompatActivity {

    private EditText mEmailAddress, mPassword;
    private String emailAddress, password;
    private TextView mSkip, mLogin;
    private Button mSignup;
    private boolean flag=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_screen);
        init();
    }

    private void init() {
        //views initialization
        mEmailAddress = findViewById(R.id.email_address);
        mPassword = findViewById(R.id.password);
        mSkip = findViewById(R.id.skip);

        //shift to login screen
        mLogin = findViewById(R.id.go_to_login);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SignupScreen.this, LoginScreen.class);
                startActivity(i);
                overridePendingTransition(R.anim.current_screen_move_mean_to_right, R.anim.incoming_screen_left_to_mean_position);
                finish();
            }
        });


        //handle signup button --> validate fields for invalid input --> Network call to register user
        mSignup = findViewById(R.id.sign_up);
        mSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //logic to reset edit fields' warnings when click
                flag=true;
                if (flag) {
                    //in order to remove warning label after resubmitting correct values in input fields
                    mEmailAddress.setError(null);
                    mPassword.setError(null);
                }
                validation();
            }
        });
    }


    //function to validate edit fields, show errors incase of invalid input
    public void validation() {
        emailAddress=mEmailAddress.getText().toString();
        password=mPassword.getText().toString();

        if (password.isEmpty()) {
            flag=false;
            mPassword.setError("enter password");
            mPassword.requestFocus();
        }

        if (emailAddress.isEmpty() | !(isEmailValid(emailAddress))) {
            flag=false;
            mEmailAddress.setError("enter email address");
            mEmailAddress.requestFocus();
        }

        if (flag){
            password= LoginScreen.stringHashSHA512(password);
            Log.d("md5hash",password);
            networkCall();
        }
    }

    public static boolean isEmailValid(String email) {
        boolean isValid = false;
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    private void networkCall() {
        final AlertDialog dialog = new ProgressDialog(SignupScreen.this);
        dialog.setMessage("wait..");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        //https://mafzal.website/apps/property_form_submission/upload_images.php
        Ion.with(SignupScreen.this).load(Constants.BASE_URL+Constants.SIGN_UP)
                .setMultipartParameter("email_address",emailAddress)
                .setMultipartParameter("password",password)
                .asString().setCallback(new FutureCallback<String>() {
            @Override
            public void onCompleted(Exception e, String result) {
                dialog.dismiss();
                if (e == null) {
                    if (result.equals("inserted")) {
                        Toast.makeText(SignupScreen.this, "Sign up Successfully!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignupScreen.this,LoginScreen.class));
                        finish();
                    } else {
                        Toast.makeText(SignupScreen.this, result, Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(SignupScreen.this, "exception: "+e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}