package com.suprema.biominisample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

public class Clockin_Startup extends AppCompatActivity {

    CardView cardViewStart;
    ProgressDialog loading;
    String adminId_, statusCode, message,CardNumber,ClockinType,UserId,FullName,PhoneNumber,EmailAddress,Organization;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clockin_startup);



        cardViewStart = findViewById(R.id.cardViewStartUp);

        cardViewStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CHECK_CLOCKIN_TYPE();
            }
        });

    }




    private void CHECK_CLOCKIN_TYPE (){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String adminId = preferences.getString("adminId", "");
        String organizationId = preferences.getString("organizationId", "");


        MobileInternetConnectionDetector dataDetector = new MobileInternetConnectionDetector(this);
        WIFIInternetConnectionDetector wifiDetector = new WIFIInternetConnectionDetector(this);

        if (!dataDetector.checkMobileInternetConn() && !wifiDetector.checkMobileInternetConn()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }


        loading = ProgressDialog.show(this, "Please wait...", "loading...", false, false);


        RequestQueue queue;
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        Network network = new BasicNetwork(new HurlStack());
        queue = new RequestQueue(cache, network);
        queue.start();

        String URL = IP.manageClockinType;

        JSONObject postparams = new JSONObject();


        try {
            postparams.put("auth", IP.Auth);
            postparams.put("adminId", adminId);
            postparams.put("action", "clockinType");
            postparams.put("organizationId", organizationId);

            Log.d("PARAMS", postparams.toString());

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, URL, postparams, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            loading.dismiss();


                            try {
                                JSONObject jsonObject = new JSONObject(response.toString());
                                statusCode = jsonObject.getString("statusCode");
                                message = jsonObject.getString("message");
                                JSONArray response_ = jsonObject.getJSONArray("response");
                                JSONObject afro = response_.getJSONObject(0);
                                ClockinType = afro.getString("ClockinType");



                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            if (statusCode.equals("200")) {


                                if(ClockinType.equalsIgnoreCase("Finger")){

                                    Intent myIntent = new Intent(Clockin_Startup.this, DefaultActivity.class);
                                    myIntent.putExtra("_CardNumber", CardNumber);
                                    myIntent.putExtra("_FullName", FullName);
                                    myIntent.putExtra("_AdminId", adminId_);
                                    myIntent.putExtra("_ClockinType", ClockinType);
                                    startActivity(myIntent);

                                } else if (ClockinType.equalsIgnoreCase("Facial")){

                                    Intent myIntent = new Intent(Clockin_Startup.this, Clockin_Facial.class);
                                    myIntent.putExtra("_CardNumber", CardNumber);
                                    myIntent.putExtra("_FullName", FullName);
                                    myIntent.putExtra("_AdminId", adminId_);
                                    myIntent.putExtra("_ClockinType", ClockinType);
                                    startActivity(myIntent);

                                } else {

                                    Intent myIntent = new Intent(Clockin_Startup.this, Clockin_Card.class);
                                    myIntent.putExtra("_CardNumber", CardNumber);
                                    myIntent.putExtra("_FullName", FullName);
                                    myIntent.putExtra("_AdminId", adminId_);
                                    myIntent.putExtra("_ClockinType", ClockinType);
                                    startActivity(myIntent);


                                }



                            } else {

                                new AlertDialog.Builder(Clockin_Startup.this)
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

                            new AlertDialog.Builder(Clockin_Startup.this)
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

                    final String AUTH = "Basic " + Base64.encodeToString((IP.APIusername + ":" + IP.APIpassword).getBytes(), Base64.NO_WRAP);
                    params.put("Authorization", AUTH);
                    params.put("Content", "application/x-www-form-urlencoded");
                    params.put("Content-Type", "application/json");
                    return params;
                }

            };

            // Add the request to the RequestQueue.
            queue.add(jsonObjectRequest);
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(0, -1, 0));
        } catch (JSONException ex) {
            Log.e("JSONError", ex.getMessage());
        } catch (Exception ex) {
            Log.e("GENERAL", ex.getMessage());
        }

    }






    private void LOGOUT1(){
        new AlertDialog.Builder(Clockin_Startup.this)
                .setMessage("Are you sure you want to logout ? ")
                .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        LOGOUT2();
                    }
                })
                .create()
                .show();
    }


    private void LOGOUT2(){


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();

        editor.remove("adminId");
        editor.remove("fullName");
        editor.remove("email");
        editor.remove("phoneNumber");
        editor.remove("role");
        editor.remove("organizationId");
        editor.remove("organizationName");
        editor.remove("organizationCategory");
        editor.remove("departmentId");
        editor.remove("departmentName");
        editor.remove("manageCard");
        editor.remove("manageClockIn");
        editor.remove("isUserLoggedIn");
        editor.commit();

        startActivity(new Intent(this, Clockin_Login.class));
        finish();
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout,menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.actionLogout){
            LOGOUT1();
        }


        return super.onOptionsItemSelected(item);
    }



}