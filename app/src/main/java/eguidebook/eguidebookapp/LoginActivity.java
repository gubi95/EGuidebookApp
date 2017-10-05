package eguidebook.eguidebookapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.icu.text.DateFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        getSupportActionBar().hide();

        final Activity objActivityCurrent = this;

        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText etLogin = (EditText) findViewById(R.id.te_login);
                EditText etPassword = (EditText) findViewById(R.id.te_password);

                etLogin.setText("happywitcher@gmail.com");
                etPassword.setText("aB34567!");

                String strUsername = etLogin.getText().toString();
                String strPassword = etPassword.getText().toString();

                boolean bIsValid = true;

                if(!PLHelpers.isEmailValid(strUsername)) {
                    bIsValid = false;
                    etLogin.setError("Adres e-mail jest nieprawidłowy");
                }

                if(PLHelpers.stringIsNullOrEmpty(strPassword)) {
                    bIsValid = false;
                    etPassword.setError("Prosze wpisać hasło");
                }

                if(!bIsValid) {
                    return;
                }

                showHideAjaxWheel(true);

                EGuidebookApplication.mUsername = strUsername;
                EGuidebookApplication.mPassword = strPassword;

                new AsyncTask<Void, Void, WebAPIManager.LoginReply>() {
                    @Override
                    protected WebAPIManager.LoginReply doInBackground(Void... voids) {
                        try {
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException e) { }
                        return new WebAPIManager().login();
                    }

                    @Override
                    protected void onPostExecute(WebAPIManager.LoginReply objLoginReply) {
                        if(!objLoginReply.isSuccess()) {
                            EGuidebookApplication.mUsername = "";
                            EGuidebookApplication.mPassword = "";
                        }
                        else {
                            EGuidebookApplication.mSpotCategories = objLoginReply.getSpotCategoriesAsArrayList() != null ?
                                    objLoginReply.getSpotCategoriesAsArrayList() : new ArrayList<WebAPIManager.SpotCategory>();

                            startActivity(new Intent(objActivityCurrent, MainActivity.class));
                        }

                        showHideAjaxWheel(false);
                    }
                }.execute();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    private void showHideAjaxWheel(boolean bShow) {
        findViewById(R.id.progressBar).setVisibility(bShow ? View.VISIBLE : View.GONE);
    }
}
