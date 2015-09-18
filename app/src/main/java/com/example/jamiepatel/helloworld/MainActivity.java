package com.example.jamiepatel.helloworld;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.support.v7.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    TextView mainTextView;
    Button mainButton;
    EditText mainEditText;
    ListView mainListView;
    JSONAdapter mJSONAdapter;
    ArrayList myNameList = new ArrayList();
    ShareActionProvider myShareActionProvider;
    ProgressDialog mDialog;

    private static final String PREFS = "prefs";
    private static final String PREF_NAME = "name";
    SharedPreferences mySharedPreferences;

    private static final String QUERY_URL = "http://openlibrary.org/search.json?q=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainTextView = (TextView) findViewById(R.id.main_textview);

        mainButton = (Button) findViewById(R.id.main_button);
        mainButton.setOnClickListener(this);

        mainEditText = (EditText) findViewById(R.id.main_edittext);

        mainListView = (ListView) findViewById(R.id.main_listview);

        mainListView.setOnItemClickListener(this);

        displayWelcome();

        mJSONAdapter = new JSONAdapter(this, getLayoutInflater());
        mainListView.setAdapter(mJSONAdapter);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Searching for Book");
        mDialog.setCancelable(false);
    }

    public void displayWelcome(){
        mySharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        String name = mySharedPreferences.getString(PREF_NAME, "");

        if (name.length()>0){
            Toast.makeText(this, "Welcome back, " + name + "!", Toast.LENGTH_LONG).show();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Hello!");
            alert.setTitle("What is your name?");

            final EditText input = new EditText(this);
            alert.setView(input);

            alert.setPositiveButton("OK", new DialogInterface.OnClickListener(){

                public void onClick(DialogInterface dialog, int which) {
                    String inputName = input.getText().toString();

                    SharedPreferences.Editor e = mySharedPreferences.edit();
                    e.putString(PREF_NAME, inputName);
                    e.commit();

                    Toast.makeText(getApplicationContext(), "Welcome, "+ inputName + "!", Toast.LENGTH_LONG).show();

                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            alert.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem shareItem = menu.findItem(R.id.menu_item_share);

        if (shareItem != null) {
            myShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        }

        setShareIntent();

        return true;
    }

    private void setShareIntent(){
        if (myShareActionProvider != null){
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Android Development");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mainTextView.getText());

            myShareActionProvider.setShareIntent(shareIntent);
        }
    }


    @Override
    public void onClick(View v) {
        queryBooks(mainEditText.getText().toString());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       JSONObject jsonObject = (JSONObject) mJSONAdapter.getItem(position);
       String coverID = jsonObject.optString("cover_i", "");


       Intent detailIntent = new Intent(this, DetailActivity.class);
       detailIntent.putExtra("coverID", coverID);

       startActivity(detailIntent);

    }

    private void queryBooks(String searchString){
        String urlString = "";
        try {
            urlString = URLEncoder.encode(searchString, "UTF-8");
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        AsyncHttpClient client = new AsyncHttpClient();
        mDialog.show();
        client.get(QUERY_URL + urlString, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(JSONObject jsonObject){
                mDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_LONG).show();

                 mJSONAdapter.updateData(jsonObject.optJSONArray("docs"));
            }

            @Override
            public void onFailure(int statusCode, Throwable throwable, JSONObject error){
                mDialog.dismiss();

                Toast.makeText(
                        getApplicationContext(),
                        "Error: " + statusCode + " " + throwable.getMessage(),
                        Toast.LENGTH_LONG).show();
                Log.e("omg android", statusCode + " " + throwable.getMessage());
            }
        });
    }
}
