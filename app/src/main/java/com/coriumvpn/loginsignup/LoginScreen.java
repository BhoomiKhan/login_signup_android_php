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

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginScreen extends AppCompatActivity {

    private EditText mEmailAddress, mPassword;
    private String emailAddress, password;
    private TextView mSkip, mRecovery, mSignup;
    private Button mLogin;
    private boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        init();
    }

    private void init() {
        //views initialization
        mEmailAddress = findViewById(R.id.email_address);
        mPassword = findViewById(R.id.password);
        mSkip = findViewById(R.id.skip);
        mRecovery = findViewById(R.id.recovery);

        //shift to sign up screen
        mSignup = findViewById(R.id.go_to_sign_up);
        mSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginScreen.this, SignupScreen.class);
                startActivity(i);
                overridePendingTransition(R.anim.signin_incoming_screen_right_to_mean_position, R.anim.signin_current_screen_move_mean_to_left);
                finish();
            }
        });

        //handle Login button --> validate fields for invalid input --> Network call to register user
        mLogin = findViewById(R.id.login);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag = true;
                if (flag) {
                    //in order to remove warning lable after resubmitting correct values in input fields
                    mEmailAddress.setError(null);
                    mPassword.setError(null);
                }
                validation();
            }
        });

        //Open Password recovery's activity
        mRecovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginScreen.this, RecoverEmail.class));
            }
        });
    }

    //validate edit text
    public void validation() {
        emailAddress = mEmailAddress.getText().toString();
        password = mPassword.getText().toString();

        if (password.isEmpty()) {
            flag = false;
            mPassword.setError("enter password");
            mPassword.requestFocus();
        }

        if (emailAddress.isEmpty() | !(isEmailValid(emailAddress))) {
            flag = false;
            mEmailAddress.setError("enter email address");
            mEmailAddress.requestFocus();
        }

        if (flag) {
            password = stringHashSHA512(password);
            Log.d("md5hash", password);
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
        final AlertDialog dialog = new ProgressDialog(LoginScreen.this);
        dialog.setMessage("wait..");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        //https://mafzal.website/apps/property_form_submission/upload_images.php
        Ion.with(LoginScreen.this).load(Constants.BASE_URL + Constants.LOGIN)
                .setMultipartParameter("email_address", emailAddress)
                .setMultipartParameter("password", password)
                .asString().setCallback(new FutureCallback<String>() {
            @Override
            public void onCompleted(Exception e, String result) {
                dialog.dismiss();
                if (e == null) {
                    if (result.equals("success")) {
                        Toast.makeText(LoginScreen.this, "Login Successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(LoginScreen.this, result, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginScreen.this, "exception is " + e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public static String stringHashSHA512(String input)
    {
        try {
            // getInstance() method is called with algorithm SHA-512
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            // Add preceding 0s to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            // return the HashText
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}