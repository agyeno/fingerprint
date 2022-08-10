package com.suprema.biominisample;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Clockin_Login extends AppCompatActivity {

    EditText emailEditText,phoneNumberEditText;
    Button loginButton;
    String statusCode,message,AdminId,FullName,Role,PhoneNumber,EmailAddress;
    String DepartmentId,DepartmentName,OrganizationId,OrganizationName,OrganizationCategory, ManageCard,ManageClockIn;
    private ProgressDialog loading;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clockin_login);


        emailEditText       = findViewById(R.id.editTextEmailLogin);
        phoneNumberEditText = findViewById(R.id.editTextPhoneLogin);
        loginButton         = findViewById(R.id.button);



        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
        String isUserLoggedIn = preferences.getString("isUserLoggedIn", "");
        String role           = preferences.getString("role", "");

        if (isUserLoggedIn.equals("true") && role.equals("Super Admin")){
            startActivity(new Intent(Clockin_Login.this, DefaultActivity.class));
            finish();
        }
        if (isUserLoggedIn.equals("true") && role.equals("Admin")){
            startActivity(new Intent(Clockin_Login.this, DefaultActivity.class));
            finish();
        }
        if (isUserLoggedIn.equals("true") && role.equals("Security")){
            startActivity(new Intent(Clockin_Login.this, Clockin_Startup.class));
            finish();
        }


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LOGIN(view);
            }
        });

    }








    private void hideSoftKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }





    private void LOGIN(View view){
        hideSoftKeyboard(view);


        if (emailEditText.getText().toString().equals("") ) {
            Toast.makeText(Clockin_Login.this, "Please provide email ", Toast.LENGTH_SHORT).show();
            return;
        }
        if ( phoneNumberEditText.getText().toString().equals("")) {
            Toast.makeText(Clockin_Login.this, "Please provide phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        MobileInternetConnectionDetector dataDetector = new MobileInternetConnectionDetector(this);
        WIFIInternetConnectionDetector wifiDetector = new WIFIInternetConnectionDetector(this);

        if (!dataDetector.checkMobileInternetConn() && !wifiDetector.checkMobileInternetConn()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }




        loading = ProgressDialog.show(this, "Please wait...", "Authenticating User...", false, false);



        // Instantiate the RequestQueue.
        RequestQueue queue;
        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
        queue = new RequestQueue(cache, network);
        queue.start();

        String URL = IP.userLogin;

        JSONObject postparams = new JSONObject();


        try {
            postparams.put("email", emailEditText.getText().toString());
            postparams.put("phoneNumber", phoneNumberEditText.getText().toString());
            postparams.put("auth", IP.Auth);

            Log.d("PARAMS", postparams.toString());


            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, URL, postparams, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            loading.dismiss();

                            try {
                                JSONObject jsonObject = new JSONObject(response.toString());
                                statusCode = jsonObject.getString("statusCode");
                                message    = jsonObject.getString("message");
                                JSONArray response_ = jsonObject.getJSONArray("response");
                                JSONObject vtom  = response_.getJSONObject(0);
                                AdminId          = vtom.getString("AdminId");
                                FullName         = vtom.getString("FullName");
                                PhoneNumber      = vtom.getString("PhoneNumber");
                                EmailAddress     = vtom.getString("EmailAddress");
                                Role             = vtom.getString("Role");
                                OrganizationId   = vtom.getString("OrganizationId");
                                OrganizationName = vtom.getString("OrganizationName");
                                OrganizationCategory = vtom.getString("OrganizationCategory");
                                DepartmentId     = vtom.getString("DepartmentId");
                                DepartmentName   = vtom.getString("DepartmentName");
                                ManageCard       = vtom.getString("ManageCard");
                                ManageClockIn    = vtom.getString("ManageClockIn");




                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            if (statusCode.equals("200") && Role.equals("Security")) {
                                // Toast.makeText(Clockin_Login.this, message, Toast.LENGTH_SHORT).show();
                                SharedPreferences preferences   = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                SharedPreferences.Editor editor = preferences.edit();

                                editor.putString("adminId", AdminId);
                                editor.putString("fullName", FullName);
                                editor.putString("email", EmailAddress);
                                editor.putString("phoneNumber", PhoneNumber);
                                editor.putString("role", Role);
                                editor.putString("organizationId", OrganizationId);

                                editor.putString("organizationName", OrganizationName);
                                editor.putString("organizationCategory", OrganizationCategory);
                                editor.putString("departmentId", DepartmentId);
                                editor.putString("departmentName", DepartmentName);
                                editor.putString("manageCard", ManageCard);
                                editor.putString("manageClockIn", ManageClockIn);
                                editor.putString("isUserLoggedIn", "true");
                                editor.commit();

                                startActivity(new Intent(Clockin_Login.this, Clockin_Startup.class));
                                finish();


                            } else if (statusCode.equals("200") && Role.equals("Super Admin") || statusCode.equals("200") && Role.equals("Admin")) {
                                // Toast.makeText(Clockin_Login.this, message, Toast.LENGTH_SHORT).show();
                                SharedPreferences preferences   = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                SharedPreferences.Editor editor = preferences.edit();

                                editor.putString("adminId", AdminId);
                                editor.putString("fullName", FullName);
                                editor.putString("email", EmailAddress);
                                editor.putString("phoneNumber", PhoneNumber);
                                editor.putString("role", Role);
                                editor.putString("organizationId", OrganizationId);

                                editor.putString("organizationName", OrganizationName);
                                editor.putString("organizationCategory", OrganizationCategory);
                                editor.putString("departmentId", DepartmentId);
                                editor.putString("departmentName", DepartmentName);
                                editor.putString("manageCard", ManageCard);
                                editor.putString("manageClockIn", ManageClockIn);
                                editor.commit();


                                Intent myIntent = new Intent(Clockin_Login.this, DefaultActivity.class);
                                myIntent.putExtra("_CardNumber", "CardNumber");
                                myIntent.putExtra("_FullName", FullName);
                                myIntent.putExtra("_AdminId", AdminId);
                                myIntent.putExtra("_ClockinType", "None");
                                startActivity(myIntent);
                         //       startActivity(new Intent(Clockin_Login.this, DefaultActivity.class));
                                finish();


                            }else {

                                new AlertDialog.Builder(Clockin_Login.this)
                                        .setMessage(message)
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        })
                                        .create()
                                        .show();

                            }

                        }


                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            loading.dismiss();

                            new AlertDialog.Builder(Clockin_Login.this)
                                    .setTitle(IP.errorMessageOops)
                                    .setMessage(IP.errorMessageSomethingWentWrong)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .create()
                                    .show();

                            // TODO: Handle error

                        }
                    }) {

                @Override
                public Map getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();

                    final String AUTH = "Basic " + Base64.encodeToString((IP.APIusername+ ":" + IP.APIpassword).getBytes(), Base64.NO_WRAP);
                    params.put("Authorization", AUTH);
                    params.put("Content", "application/x-www-form-urlencoded");
                    params.put("Content-Type", "application/json");
                    return params;
                }

            };

// Add the request to the RequestQueue.
            queue.add(jsonObjectRequest);
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(0,-1,0));
        } catch (JSONException ex) {
            Log.e("JSONError", ex.getMessage());
        } catch (Exception ex) {
            Log.e("GENERAL", ex.getMessage());
        }

    }



}