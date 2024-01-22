package com.example.focus;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout layoutTextFullname, layoutTextUsername, layoutTextPassword, layoutTextConfirmPassword;
    private TextInputEditText edtInputFullname, edtInputUsername, edtInputPassword, edtInputConfirmPassword;

    private Button btnRegister;

    TextView txtLinkLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        init();

    }


    private void init(){

        btnRegister = findViewById(R.id.btnRegister);

        layoutTextUsername = findViewById(R.id.layoutTextUsername);
        layoutTextPassword = findViewById(R.id.layoutTextPassword);
        layoutTextConfirmPassword = findViewById(R.id.layoutTextConfirmPassword);
        layoutTextFullname = findViewById(R.id.layoutTextFullname);

        edtInputUsername = findViewById(R.id.edtInputUsername);
        edtInputPassword = findViewById(R.id.edtInputPassword);
        edtInputConfirmPassword = findViewById(R.id.edtInputConfirmPassword);
        edtInputFullname = findViewById(R.id.edtInputFullname);

        txtLinkLogin = findViewById(R.id.txtLinkLogin);

        txtLinkLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        edtInputFullname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!edtInputFullname.getText().toString().isEmpty()){
                    layoutTextFullname.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edtInputUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(!edtInputUsername.getText().toString().isEmpty()){
                    layoutTextUsername.setErrorEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edtInputPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(edtInputPassword.getText().toString().length() > 7){
                    layoutTextPassword.setErrorEnabled(false);
                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edtInputConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(edtInputConfirmPassword.getText().toString().equals(edtInputPassword.getText().toString())){
                    layoutTextConfirmPassword.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        btnRegister.setOnClickListener(v -> {
            if(validate()){
                register();
            }
        });



    }

    private boolean validate(){

        String full_name = edtInputFullname.getText().toString();
        String username = edtInputUsername.getText().toString();
        String password = edtInputPassword.getText().toString();
        String confirm_password = edtInputConfirmPassword.getText().toString();


        if(full_name.isEmpty()){
            layoutTextFullname.setErrorEnabled(true);
            layoutTextFullname.setError("Full Name is Required");
            return false;
        }

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

        if(confirm_password.isEmpty()){
            layoutTextConfirmPassword.setErrorEnabled(true);
            layoutTextConfirmPassword.setError("Confirm Password is Required");
            return false;
        }

        if (password.length() < 8) {
            layoutTextPassword.setErrorEnabled(true);
            layoutTextPassword.setError("Required at least 8 character");
            return false;
        }

        if(!confirm_password.equals(password)){
            layoutTextConfirmPassword.setErrorEnabled(true);
            layoutTextConfirmPassword.setError("Password Does not Match");
            return false;
        }

        return true;

    }

    private void register(){

        StringRequest request = new StringRequest(Request.Method.POST, API.register_api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getBoolean("success")) {

                        JSONObject userObject = jsonObject.getJSONObject("user");
                        SharedPreferences userPref = RegisterActivity.this.getApplicationContext().getSharedPreferences("user", RegisterActivity.this.MODE_PRIVATE);
                        SharedPreferences.Editor editor = userPref.edit();

                        editor.putString("token", jsonObject.getString("token"));
                        editor.putString("name", userObject.getString("name"));
                        editor.putString("username", userObject.getString("username"));
                        editor.putString("collectors_id", userObject.getString("collectors_id"));

                        editor.putBoolean("isLoggedIn", true);
                        editor.apply();

                        startActivity(new Intent(RegisterActivity.this, StartingActivity.class));
                        RegisterActivity.this.finish();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, error -> {

            Toast.makeText(RegisterActivity.this, "Error in Connection", Toast.LENGTH_SHORT).show();
            error.printStackTrace();

        }) {

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("name", edtInputFullname.getText().toString().trim());
                map.put("username", edtInputUsername.getText().toString().trim());
                map.put("password", edtInputPassword.getText().toString());
                return map;
            }
        };

        //test

        RequestQueue queue = Volley.newRequestQueue(this);
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);

    }

}