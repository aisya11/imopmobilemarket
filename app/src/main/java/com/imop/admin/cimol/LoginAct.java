package com.imop.admin.cimol;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.imop.admin.server.Config;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;

import static com.imop.admin.server.Config.URL_BASE;

public class LoginAct extends AppCompatActivity {
    Button daftar, login, editAdmin;
    Intent a;
    EditText email, password;
    String url, success, id_owner;
    SessionManager session;

    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new SessionManager(getApplicationContext());

        if (session.isLoggedIn()) {
            startActivity(new Intent(LoginAct.this, MainActivity.class));
        }
        //buat db
        DatabaseHandler dbHelper = new DatabaseHandler(this);
        dbHelper.createDataBase();

        login = (Button) findViewById(R.id.login);
        //daftar = (Button) findViewById(R.id.daftar);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);

        //loading bar
        pDialog = new ProgressDialog(LoginAct.this);
        pDialog.setMessage("Loading...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);

        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (email.getText().toString().trim().length() > 0 && password.getText().toString().trim().length() > 0) {
                    if (isValidEmail(email.getText().toString().trim())) {
                        login(email.getText().toString().trim(), password.getText().toString().trim());
                    } else {
                        Toast.makeText(LoginAct.this, "Format email salah!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Username/password masih kosong!", Toast.LENGTH_LONG).show();
                }


            }
        });

    }

    public void login(String username, String password) {
        pDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("email_owner", username);
        params.add("password", password);
        client.post(Config.URL_BASE_API + "/login_owner.php", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                pDialog.dismiss();
                try {
                    String success = response.getString("success");
                    if (success.equalsIgnoreCase("1")) {
                        JSONArray array = response.getJSONArray("login");
                        JSONObject object = array.getJSONObject(0);
                        String idOwner = object.getString("id_owner");
                        String namaOwner = object.getString("nama_owner");
                        String emailOwner = object.getString("email_owner");
                        session.createLoginSession(namaOwner, emailOwner, idOwner);

                        a = new Intent(LoginAct.this, MainActivity.class);
                        startActivity(a);
                        finish();

                    } else {
                        Toast.makeText(LoginAct.this, "Cek kembali email dan password!", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                pDialog.dismiss();
                throwable.printStackTrace();
                Log.e("TAG", errorResponse.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                pDialog.dismiss();
                throwable.printStackTrace();
                Log.e("TAG", "Login error : " + responseString);
            }
        });


    }

    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
