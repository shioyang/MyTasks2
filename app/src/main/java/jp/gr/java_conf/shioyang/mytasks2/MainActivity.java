package jp.gr.java_conf.shioyang.mytasks2;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;

import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String APPLICATION_NAME = "MyTask2/1.0";

    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
    public static final int REQUEST_AUTHORIZATION = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 2;

    final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    final GsonFactory gsonFactory = GsonFactory.getDefaultInstance();
    
    GoogleAccountCredential credential;
    Tasks service;

    ListView listView;
    ArrayAdapter<String> adapter;
    List<String> tasksList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Google Accounts
        credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(TasksScopes.TASKS));
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        credential.setSelectedAccountName(prefs.getString(PREF_ACCOUNT_NAME, null));

        // Tasks client
        service = new Tasks.Builder(httpTransport, gsonFactory, credential).setApplicationName(APPLICATION_NAME).build();

        // ListView
        listView = (ListView) findViewById(R.id.listView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkGooglePlayServicesAvailability()) {
            haveGooglePlayServices();
        }
    }

    // ----------
    private boolean checkGooglePlayServicesAvailability() {
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        }
        return true;
    }

    private void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(connectionStatusCode, MainActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    private void haveGooglePlayServices() {
        if (credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            AsyncLoadTasks.run(this);
        }
    }

    private void chooseAccount() {
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    // ----------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK) {
                    haveGooglePlayServices();
                } else {
                    checkGooglePlayServicesAvailability();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        AsyncLoadTasks.run(this);
                    }
                }
                break;
            case REQUEST_AUTHORIZATION: // In AsyncLoadTasks, when access right is needed. UserRecoverableException
                if (resultCode == Activity.RESULT_OK) {
                    AsyncLoadTasks.run(this);
                } else {
                    chooseAccount();
                }
                break;
        }
    }

    // ----------
    void refreshView() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tasksList);
        listView.setAdapter(adapter);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
