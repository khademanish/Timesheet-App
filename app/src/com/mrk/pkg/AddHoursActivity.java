package com.mrk.pkg;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;

import org.json.JSONArray;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddHoursActivity extends SalesforceActivity {

    private RestClient client;
    private ArrayAdapter<String> adapter;
    private Map<String, String> enhancementMap = new HashMap<>();
    private ArrayList<String> enhancements = new ArrayList<String>();
    private ArrayAdapter<String> userAdapter;
    private Map<String, String> userMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hours);

    }

    public void onResume() {
        // Hide everything until we are logged in
		findViewById(R.id.drawer).setVisibility(View.INVISIBLE);


		// Create list adapter

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.enhancement);
        textView.setAdapter(adapter);

        userAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        AutoCompleteTextView userTextView = (AutoCompleteTextView)findViewById(R.id.user);
        userTextView.setAdapter(userAdapter);
        super.onResume();
    }

    @Override
    public void onResume(RestClient client) {
        // Keeping reference to rest client
        this.client = client;
        try {
            sendRequest("Select Id, Name From pslPractice__Enhancement__c");
            sendRequest("Select Id, Name From User");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // Show everything
        findViewById(R.id.drawer).setVisibility(View.VISIBLE);
    }

    public void onsaveClick(View v) throws UnsupportedEncodingException{
        sendRequest();
    }

    private void sendRequest() throws UnsupportedEncodingException{
        AutoCompleteTextView userTextView = (AutoCompleteTextView)findViewById(R.id.user);
        AutoCompleteTextView enhTextView = (AutoCompleteTextView)findViewById(R.id.enhancement);
        EditText bugId = (EditText)findViewById(R.id.bugid);
        EditText description = (EditText)findViewById(R.id.description);
        Log.d(AddHoursActivity.class.getSimpleName(),enhancementMap.toString());
        Log.d(AddHoursActivity.class.getSimpleName(),userMap.toString());
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("pslPractice__Enhancement__c",(Object)(enhancementMap.get(""+enhTextView.getText())));
        dataMap.put("pslPractice__User__c",(Object)(userMap.get(""+userTextView.getText())));
        dataMap.put("pslPractice__Bug_ID__c",(Object)(""+bugId.getText()));
        dataMap.put("pslPractice__Description__c",(Object)(""+description.getText()));

        Log.d(AddHoursActivity.class.getSimpleName(), dataMap.toString());
        try {
            RestRequest restRequest = RestRequest.getRequestForUpsert(ApiVersionStrings.getVersionNumber(this), "pslPractice__Hour__c", "Id", null,dataMap);

            client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
                @Override
                public void onSuccess(RestRequest request, final RestResponse result) {
                    result.consumeQuietly(); // consume before going back to main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.d(AddHoursActivity.class.getSimpleName(), result.toString());

                            } catch (Exception e) {
                                onError(e);
                            }
                        }
                    });
                }

                @Override
                public void onError(final Exception exception) {
                    Log.d(MainActivity.class.getSimpleName(),"Error got");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AddHoursActivity.this,
                                    AddHoursActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void sendRequest(String soql) throws UnsupportedEncodingException {
        RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);
		/*Map<String, Object> testMap = new HashMap<String, Object>();
		testMap.put("pslPractice__Test_Field__c", (Object)("test Data"));
		RestRequest restRequest = null;
		try {
			Log.d(MainActivity.class.getSimpleName(),"Inside Call");
			restRequest = RestRequest.getRequestForUpsert(ApiVersionStrings.getVersionNumber(this), "pslPractice__POC1__c", "Id", null,testMap);
		} catch (IOException e) {
			Log.d(MainActivity.class.getSimpleName(),"Exception: ");
			e.printStackTrace();
		}*/

        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                Log.d(AddHoursActivity.class.getSimpleName(), result.toString());
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            adapter.clear();
                            JSONArray records = result.asJSONObject().getJSONArray("records");
                            for (int i = 0; i < records.length(); i++) {
                                if(records.getJSONObject(i).getString("attributes").contains("pslPractice__Enhancement__c")){
                                    adapter.add(records.getJSONObject(i).getString("Name"));
                                    enhancementMap.put(records.getJSONObject(i).getString("Name"),records.getJSONObject(i).getString("Id"));
                                }
                                if(records.getJSONObject(i).getString("attributes").contains("User")){
                                    userAdapter.add(records.getJSONObject(i).getString("Name"));
                                    userMap.put(records.getJSONObject(i).getString("Name"),records.getJSONObject(i).getString("Id"));
                                }
                            }
                        } catch (Exception e) {
                            onError(e);
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception exception) {
                Log.d(MainActivity.class.getSimpleName(),"Error got");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AddHoursActivity.this,
                                AddHoursActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
