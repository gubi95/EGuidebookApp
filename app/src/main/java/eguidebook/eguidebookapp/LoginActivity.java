package eguidebook.eguidebookapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.text.DateFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import mehdi.sakout.fancybuttons.FancyButton;

public class LoginActivity extends AppCompatActivity {
    public boolean _bIsRegisterMode = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        _bIsRegisterMode = false;
        this.setViewDependsOnMode();

        getSupportActionBar().hide();

        final Activity objActivityCurrent = this;

        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View view) {
                EGuidebookApplication.mGPSTrackerService = new GPSTrackerService(objActivityCurrent);
                if (!EGuidebookApplication.mGPSTrackerService.canGetLocation()) {
                    return;
                }

                final EditText etLogin = findViewById(R.id.te_login);
                final EditText etPassword = findViewById(R.id.te_password);

                if(getResources().getBoolean(R.bool.isTestEnvironment)) {
                    etLogin.setText(getString(R.string.defUsername));
                    etPassword.setText(getString(R.string.defPassword));
                }

                final String strUsername = etLogin.getText().toString();
                final String strPassword = etPassword.getText().toString();

                boolean bIsValid = true;

                if (!PLHelpers.isEmailValid(strUsername)) {
                    bIsValid = false;
                    etLogin.setError("Adres e-mail jest nieprawidłowy");
                }

                if (PLHelpers.stringIsNullOrEmpty(strPassword)) {
                    bIsValid = false;
                    etPassword.setError("Proszę wpisać hasło");
                }

                if (!bIsValid) {
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
                        } catch (InterruptedException e) { }
                        return _bIsRegisterMode ? new WebAPIManager().register(strUsername, strPassword) : new WebAPIManager().login();
                    }

                    @Override
                    protected void onPostExecute(WebAPIManager.LoginReply objLoginReply) {
                        if (!objLoginReply.isSuccess()) {
                            EGuidebookApplication.mUsername = "";
                            EGuidebookApplication.mPassword = "";

                            switch(objLoginReply.Code) {
                                case WebAPIManager.CODE_USER_ALREADY_EXISTS:
                                    etLogin.setError("Podany użytkownik już istnieje");
                                    break;
                                case WebAPIManager.CODE_INCORRECT_USERNAME:
                                    etLogin.setError("Adres e-mail jest nieprawidłowy");
                                    break;
                                case WebAPIManager.CODE_INCORRECT_PASSWORD:
                                    etPassword.setError("Hasło powinno zawierać conajmniej 1 wielką literę oraz 1 znak specjalny");
                                    break;
                            }

                        } else {
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

    @Override
    public void onBackPressed() {
        if(this._bIsRegisterMode) {
            this._bIsRegisterMode = false;
            setViewDependsOnMode();
        }
        else {
            super.onBackPressed();
        }
    }

    public void onRegisterClick(View view) {
        this._bIsRegisterMode = true;
        this.setViewDependsOnMode();
    }

    public void setViewDependsOnMode() {
        ((FancyButton) findViewById(R.id.btn_login)).setText(this._bIsRegisterMode ? "Zarejestruj" : "Zaloguj");
        ((TextView)findViewById(R.id.tv_register)).setTextColor(Color.parseColor(this._bIsRegisterMode ? "#00FFFFFF" : "#FFFFFF"));

        EditText etLogin = (EditText) findViewById(R.id.te_login);
        EditText etPassword = (EditText) findViewById(R.id.te_password);

        etLogin.setText("");
        etLogin.setError(null);

        etPassword.setText("");
        etPassword.setError(null);
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
