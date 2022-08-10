package com.suprema.biominisample;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.util.RSReader;
import com.telpo.tps550.api.util.ReaderUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Clockin_Card extends AppCompatActivity implements TextToSpeech.OnInitListener {
    TextView responseTextView;
    EditText temperatureEditText,cardNumberEditText;
    Button submitButton;
    ProgressDialog loading;
    String statusCode,message,adminId,speak,cardOwner_,cardNumber_,clockinType_;
    FloatingActionButton floatingActionButton;
    ImageView imageView;
    ProgressBar progressBar;

    private TextToSpeech tts;

    ReaderUtils readerUtils;
    RSReader rsReader;
    boolean flag=true;
    byte[] bytes=null;
    List<byte[]> bytearray;
    String context;
    int number=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clockin_card);



        setTitle("Clock In System");

        tts = new TextToSpeech(this, this);
        readerUtils=new ReaderUtils();
        rsReader=new RSReader();
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);


        responseTextView     = findViewById(R.id.textViewClockOutResponse);
        temperatureEditText  = findViewById(R.id.editTextTemperatureClockOut);
        cardNumberEditText   = findViewById(R.id.editTextCardNoClockOut);
        submitButton         = findViewById(R.id.buttonSaveClockOut);
        floatingActionButton = findViewById(R.id.floatingActionButtonClockOut);
        imageView            = findViewById(R.id.imageView4);
        progressBar          = findViewById(R.id.progressBar5);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);



        SharedPreferences preferences  = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        adminId  = preferences.getString("adminId", "");
        Intent intent = getIntent();
        cardOwner_ = intent.getStringExtra("_FullName");
        cardNumber_ = intent.getStringExtra("_CardNumber");
        clockinType_ = intent.getStringExtra("_ClockinType");

        if(clockinType_.equals("Finger")){
             cardNumberEditText.setText(cardNumber_);
            CLOCK_OUT();

        } else if(clockinType_.equals("Facial")){
            cardNumberEditText.setText(cardNumber_);
            CLOCK_OUT();

        } else {

        }

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // new IntentIntegrator(Clockin_Card.this).setCaptureActivity(ScannerActivity.class).initiateScan();
            }
        });




        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CLOCK_OUT();
            }
        });





    }






//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        //We will get scan results here
//        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//        //check for null
//        if (result != null) {
//            if (result.getContents() == null) {
//                Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_LONG).show();
//            } else {
//                //show dialogue with result
//                showResultDialogue(result.getContents());
//            }
//        } else {
//            // This is important, otherwise the result will not be passed to the fragment
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }




    //method to construct dialogue with scan results
    public void showResultDialogue(final String result) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        cardNumberEditText.setText(result);
        //San result
        if (!cardNumberEditText.getText().toString().equals("")){
            CLOCK_OUT();
        }
    }







    private void hideSoftKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }





    private void CLOCK_OUT(){
        //hideSoftKeyboard(view);

        responseTextView.setText("");
        imageView.setVisibility(View.GONE);


        if (cardNumberEditText.getText().toString().equals("") ) {
            Toast.makeText(Clockin_Card.this, "Please scan ID or provide ID ", Toast.LENGTH_SHORT).show();
            return;
        }

        MobileInternetConnectionDetector dataDetector = new MobileInternetConnectionDetector(this);
        WIFIInternetConnectionDetector wifiDetector = new WIFIInternetConnectionDetector(this);

        if (!dataDetector.checkMobileInternetConn() && !wifiDetector.checkMobileInternetConn()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }


        //loading = ProgressDialog.show(this, "Please wait...", "loading...", false, true);
        progressBar.setVisibility(View.VISIBLE);



        // Instantiate the RequestQueue.
        RequestQueue queue;
        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
        queue = new RequestQueue(cache, network);
        queue.start();

        String URL = IP.userClocking;

        JSONObject postparams = new JSONObject();


        try {
            postparams.put("cardNumber", cardNumberEditText.getText().toString());
            postparams.put("auth", IP.Auth);
            postparams.put("adminId", adminId);
            postparams.put("temperature", temperatureEditText.getText().toString());



            Log.d("PARAMS", postparams.toString());

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, URL, postparams, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            //loading.dismiss();
                            progressBar.setVisibility(View.GONE);


                            try {
                                JSONObject jsonObject = new JSONObject(response.toString());
                                statusCode = jsonObject.getString("statusCode");
                                message    = jsonObject.getString("message");

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            if (statusCode.equals("200")) {
                                if (speak.equals("yes")){ speakOut(message); }
                                cardNumberEditText.setText("");
                                responseTextView.setText(message);
                                responseTextView.setTextColor(Color.parseColor("#8BC34A"));
                                imageView.setVisibility(View.VISIBLE);
                                imageView.setBackgroundResource(R.drawable.ic_correct_green);
                                MediaPlayer mPlayer = MediaPlayer.create(Clockin_Card.this, R.raw.positive);
                                Intent myIntent = new Intent(Clockin_Card.this, Clockin_Startup.class);
                                startActivity(myIntent);
                                finish();
                                //mPlayer.start();


                            } else {

                                if (speak.equals("yes")){ speakOut(message); }
                                cardNumberEditText.setText("");
                                responseTextView.setText(message);
                                responseTextView.setTextColor(Color.parseColor("#D75710"));
                                imageView.setVisibility(View.VISIBLE);
                                imageView.setBackgroundResource(R.drawable.ic_cancel_red);
                                MediaPlayer mPlayer = MediaPlayer.create(Clockin_Card.this, R.raw.negative);
                                //mPlayer.start();

                            }


                        }




                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //loading.dismiss();
                            progressBar.setVisibility(View.GONE);

                            new AlertDialog.Builder(Clockin_Card.this)
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








    public void startTelpoScanner(){


        try {
            Log.e("yw","set start");
            bytes=rsReader.RsConfig(Clockin_Card.this, 115200, (byte) 0x08, (byte) 0x02, (byte) 0x01, (byte) 0x00);
            Log.e("yw","set end");
            if (bytes[0]==0){
                //Toast.makeText(getApplicationContext(),"set success",Toast.LENGTH_SHORT).show();
            }else {
                //Toast.makeText(getApplicationContext(),"set fail",Toast.LENGTH_SHORT).show();
            }
        } catch (TelpoException e) {
            e.printStackTrace();
            //Toast.makeText(getApplicationContext(),"set fail",Toast.LENGTH_SHORT).show();
        }



        final byte[] bytes = {(byte) 0xff,0x4d,0x0d,0x38,0x36,0x31,0x30,0x30,0x32,0x38,0x2e};
        try {
            rsReader.rs_write(getApplicationContext(),bytes,bytes.length);
        } catch (TelpoException e) {
            e.printStackTrace();
        }


        new Thread(new Runnable() {
            @Override
            public void run() {
                while (flag){
                    try {
                        Log.e("yw","start");
                        //bytes=readerUtils.rs_read(ClockOut.this,1024*1000,5*100);
                        bytearray = rsReader.rs_read(Clockin_Card.this, 1024 * 1000, 5 * 100);
                        Log.e("yw","end");
                        if (bytearray !=null && bytearray.size()>0){
                            String string1="";
                            for (int j = 0; j< bytearray.get(bytearray.size()-1).length; j++){
                                string1=string1+bytearray.get(bytearray.size()-1)[j];
                            }
                            Log.e("yw_byte",string1);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        number++;
                                        if (number%2==0){
                                            cardNumberEditText.setText("");
                                            cardNumberEditText.setTextColor(getResources().getColor(R.color.colorPrimary));
                                            context =new String(bytearray.get(bytearray.size()-1));
                                            cardNumberEditText.setText(context);
                                            CLOCK_OUT();

                                            //cardNumberEditText.setTextColor(getResources().getColor(R.color.colorAccent));

                                        }else {
                                            cardNumberEditText.setText("");
                                            cardNumberEditText.setTextColor(getResources().getColor(R.color.colorPrimary));
                                            context = new String(bytearray.get(bytearray.size()-1));
                                            cardNumberEditText.setText(context);
                                            CLOCK_OUT();
                                        }

                                        //  context = new String(bytes,"UTF-8");
                                        //  context =new String(bytearray.get(bytearray.size()-1));
                                        //  cardNumberEditText.setText(context);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                        }
                    } catch (TelpoException e) {
                        e.printStackTrace();
                        Log.e("yw_error",e.toString());
                    }
                }
            }
        }).start();
    }







    @Override
    protected void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        flag=false;
        super.onDestroy();

    }




    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }



    @Override
    protected void onResume() {
        startTelpoScanner();
        super.onResume();
    }


    private boolean hasFlash() {
        return getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }












    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                speak = "yes";
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }



    private void speakOut(String message ) {
        //String text = editText.getText().toString();
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
    }




}