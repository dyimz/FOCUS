package org.focus.app.Auth;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.focus.app.Constants.API;
import org.focus.app.HomeActivity;
import org.focus.app.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    TextInputLayout layoutTextUsername, layoutTextPassword;
    TextInputEditText edtTextUsername, edtTextPassword;

    Button btnLogin;

    TextView txtLinkRegister;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();

    }

    private void init(){

        btnLogin = findViewById(R.id.btnLogin);

        layoutTextUsername = findViewById(R.id.layoutTextUsername);
        layoutTextPassword = findViewById(R.id.layoutTextPassword);

        edtTextUsername = findViewById(R.id.edtInputUsername);
        edtTextPassword = findViewById(R.id.edtInputPassword);

        txtLinkRegister = findViewById(R.id.txtLinkRegister);

        txtLinkRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()){
                    login();
                }
            }
        });

    }


    private boolean validate(){

        String username = edtTextUsername.getText().toString();
        String password = edtTextPassword.getText().toString();

        if(username.isEmpty()){
            layoutTextUsername.setErrorEnabled(true);
            layoutTextUsername.setError("Username is Required");
            return false;
        }

        if(password.isEmpty()){
            layoutTextPassword.setErrorEnabled(true);
            layoutTextPassword.setError("Password is Required");
            return false;
        }

        return true;
    }

    private void login(){

        StringRequest request = new StringRequest(Request.Method.POST, API.login_api, response -> {

            try {
                JSONObject jsonObject = new JSONObject(response);

                if (jsonObject.getBoolean("success")) {
                    JSONObject userObject = jsonObject.getJSONObject("user");

                    SharedPreferences userPref = this.getApplicationContext().getSharedPreferences("user", this.MODE_PRIVATE);
                    SharedPreferences.Editor editor = userPref.edit();

                    editor.putString("token", jsonObject.getString("token"));
                    editor.putString("name", userObject.getString("name"));
                    editor.putString("username", userObject.getString("username"));
                    editor.putString("collectors_id", userObject.getString("collectors_id"));

                    editor.putBoolean("isLoggedIn", true);
                    editor.putBoolean("isRegistered", true);

                    editor.apply();

                    //if Success
                    Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomeActivity.class));
                    this.finish();
                }

            } catch (JSONException e) {
                Toast.makeText(this, "Please Try Again", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        }, error -> {
            Toast.makeText(this, "Error in Connection", Toast.LENGTH_SHORT).show();
            error.printStackTrace();
        }) {

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("username", edtTextUsername.getText().toString().trim());
                map.put("password", edtTextPassword.getText().toString());
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);

    }



}