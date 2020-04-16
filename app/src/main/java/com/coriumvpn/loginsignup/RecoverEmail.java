package com.coriumvpn.loginsignup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class RecoverEmail extends AppCompatActivity {
    private EditText mEmailAddress, mNewPassword, mConfirmPassword, mEnterCode;
    private String multiPurpose = "nothing", emailAddress, newPassword, confirmNewPassword, container, enterCode, clickType = Constants.CHECK_EMAIL, userId, userType = "instructor";
    private Button changePassword;

    /*
    First user will enter the current password, if current password will be correct then the two fields will be showed to enter the for new password
    'clickType' --> Check the xml of this activity, that is containing 3 input fields, 1 for old password, 2 others for new & confirm password, this variable will use to set the visibility of these inpur fields from show to hide and vice versa
    'passwordContainer'--> there will be 2 times network call in this activity, 1 for to verify the old password $ second for to update new password into database, in each network call there we are sending a value of password, in first  case password will be the 'current passwrd' and in the second case the password will be 'new password' so passwordContainer will contain the either valaues
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_email);
        init();
    }

    private void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
//        mActionBarToolbar.setTitle("My title");
//        setSupportActionBar(mActionBarToolbar);

        //get instructor id from home screen, as ins_id will use to update the password of a particular instructor on database
//        userId = getIntent().getStringExtra("ins_id");
        mEmailAddress = (EditText) findViewById(R.id.email_address);
        mEnterCode = (EditText) findViewById(R.id.enter_code);
        mNewPassword = (EditText) findViewById(R.id.new_password);
        mConfirmPassword = (EditText) findViewById(R.id.confirm_password);
        changePassword = (Button) findViewById(R.id.change_password);
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validation();
            }
        });
    }

    /*There are 4 edit text fields
    Step#1: First for email address to verify either user is registered or not in the remote database, after confirmation API will send a code to the registered email address.
    Step#2: After email confirmation, API sends a code into registered email address, second field represents the code
    Step#3: On the third step, user will have to put new password*/
    private void validation() {
        if (clickType.equals(Constants.CHECK_EMAIL)) {
            emailAddress = mEmailAddress.getText().toString();
            if (LoginScreen.isEmailValid(emailAddress)) {
                handleNetworkCalls();
                clickType = Constants.CHECK_EMAIL;
            } else {
                mEmailAddress.setError("Enter valid email address");
                mEmailAddress.setFocusable(true);
            }
        }

        //When user will type his/her old password
        else if (clickType.equals(Constants.CHECK_CODE)) {
            enterCode = mEnterCode.getText().toString();
            if (!enterCode.isEmpty()) {
                multiPurpose = enterCode;
                handleNetworkCalls();
                clickType = Constants.CHECK_CODE;
            } else {
                mEnterCode.setError("Enter a code");
                mEnterCode.setFocusable(true);
            }
        }

        //when user will set his/her new password
        else if (clickType.equals(Constants.UPDATE_PASSWORD)) {
            newPassword = mNewPassword.getText().toString();
            confirmNewPassword = mConfirmPassword.getText().toString();
            if (!newPassword.isEmpty()) {
                if (!confirmNewPassword.isEmpty()) {
                    if (newPassword.equals(confirmNewPassword)) {
                        clickType = Constants.UPDATE_PASSWORD;
                        multiPurpose = newPassword;
                        multiPurpose = LoginScreen.stringHashSHA512(newPassword);
                        handleNetworkCalls();
                    } else {
                        Toast.makeText(this, "Password doesn't match", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mConfirmPassword.setError("Enter confirm password");
                    mConfirmPassword.setFocusable(true);
                    Toast.makeText(this, "new password", Toast.LENGTH_SHORT).show();
                }
            } else {
                mNewPassword.setError("Enter new password");
                mNewPassword.setFocusable(true);
                Toast.makeText(this, "confirm", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*This only 1 API will handle all network calls, for password recovery, there will be 3 times when app will send a request to the server
    First API call triggers to verify email address, second API call verifies the code and third API call updates the password
    There are 3 variables, that take part into the API call. *multipurpose* variable will hold the null, code & new password. *email_address*
    will hold email address and *click_type* will hold "check_email" , "check_password" & "update_password" respectively
    */
    private void handleNetworkCalls() {
        final ProgressDialog loadingdialog;
        loadingdialog = new ProgressDialog(RecoverEmail.this);
        loadingdialog.setTitle("Checking");
        loadingdialog.setMessage("Please wait..");
        loadingdialog.show();

        Log.d("valuesvariables", emailAddress);
        Log.d("valuesvariables", clickType);
        Log.d("valuesvariables", multiPurpose);

        Ion.with(RecoverEmail.this)
                .load(Constants.BASE_URL + Constants.RECOVER_PASSWORD)
                .setBodyParameter("email_address", emailAddress)
                .setBodyParameter("click_type", clickType)
                .setBodyParameter("multi_purpose", multiPurpose)
                .asString().setCallback(new FutureCallback<String>() {
            @Override
            public void onCompleted(Exception e, String result) {
                if (e == null && result != null) {
                    if (loadingdialog.isShowing()) {
                        loadingdialog.dismiss();
                    }
                    if (clickType.equals(Constants.CHECK_EMAIL)) {
                        if (result.equals("done")) {
                            mEnterCode.setVisibility(View.VISIBLE);
                            mEmailAddress.setVisibility(View.GONE);
                            //After email verification and sending a code, now change the status of clickType variable from "check_email" to "verify_code"
                            clickType = Constants.CHECK_CODE;
                        } else {
                            Toast.makeText(RecoverEmail.this, "email is not registed", Toast.LENGTH_SHORT).show();
                        }
                    } else if (clickType.equals(Constants.CHECK_CODE)) {
                        if (result.equals("correct code")) {
                            //After code verification, now change the status of clickType variable from "check_code" to "update_password"
                            clickType = Constants.UPDATE_PASSWORD;
                            mEnterCode.setVisibility(View.GONE);
                            mNewPassword.setVisibility(View.VISIBLE);
                            mConfirmPassword.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(RecoverEmail.this, "Incorrect Code", Toast.LENGTH_SHORT).show();
                        }
                    } else if (clickType.equals(Constants.UPDATE_PASSWORD)) {
                        if (result.equals("updated")) {
                            finish();
                            Toast.makeText(RecoverEmail.this, "Password has been updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RecoverEmail.this, "Fail", Toast.LENGTH_SHORT).show();
                        }
                    }
                    return;
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        finish();
        return true;
    }
}