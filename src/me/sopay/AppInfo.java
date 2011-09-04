package me.sopay;

/*
 * this was mainly for testing
 * I don't think we'll actually need this file for the app
 * 
 * 
 * 
 * 
 * 
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class AppInfo extends Activity {

	DefaultHttpClient http_client = new DefaultHttpClient();
	private static final String site = "cgibbs-test.sopay-me.appspot.com";
	private static final String TAG = "SoPayMeActivity-AppInfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.app_info);
            
            
    }

    @Override
    protected void onResume() {
            super.onResume();
            Intent intent = getIntent();
            AccountManager accountManager = AccountManager.get(getApplicationContext());
            Account account = (Account)intent.getExtras().get("account");
            accountManager.getAuthToken(account, "ah", false, new GetAuthTokenCallback(), null);
    }
    
    
    private class GetAuthTokenCallback implements AccountManagerCallback {
        public void run(AccountManagerFuture result) {
    		Bundle bundle;
            try {
                    bundle = (Bundle) result.getResult();
                    Intent intent = (Intent)bundle.get(AccountManager.KEY_INTENT);
                    if(intent != null) {
                        // User input required
                    	Log.i(TAG, "need user input");
                        startActivity(intent);
                    } else {
                        onGetAuthToken(bundle);
                    }
            } catch (OperationCanceledException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            } catch (AuthenticatorException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }
        }
    }
    
    protected void onGetAuthToken(Bundle bundle) {
    	Log.i(TAG, "onGetAuthToken");
    	String auth_token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
    	
        new GetCookieTask().execute(auth_token);
    }
    
    private class GetCookieTask extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... tokens) {
                try {
                        // Don't follow redirects
                        http_client.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
                        
                        HttpGet http_get = new HttpGet("https://" + site + "/_ah/login?continue=http://localhost/&auth=" + tokens[0]);
                        HttpResponse response;
                        response = http_client.execute(http_get);
                        if(response.getStatusLine().getStatusCode() != 302)
                                // Response should be a redirect
                                return false;
                        
                        for(Cookie cookie : http_client.getCookieStore().getCookies()) {
                            Log.i(TAG, cookie.getName());
                        	if(cookie.getName().equals("ACSID"))
                        		return true;
                        }
                } catch (ClientProtocolException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                } finally {
                        http_client.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
                }
                return false;
        }	
        
	    protected void onPostExecute(Boolean result) {
	    	
	    	/*
	    	 * add order submit request info here
	    	 */
	    	
//	        String fullsite = "https://" + site + "/rpc?action=GetAll";
//	        Log.i(TAG, "trying to reach " + fullsite);
	    	
//	    	new AuthenticatedRequestTask().execute(fullsite);
	    }

    }

    private class AuthenticatedRequestTask extends AsyncTask<String, Void, HttpResponse> {
        
    	@Override
        protected HttpResponse doInBackground(String... urls) {
                
    		Log.i(TAG, "AuthenticatedRequestTask doInBackground");
    			try {
                        HttpGet http_get = new HttpGet(urls[0]);
                        return http_client.execute(http_get);
                } catch (ClientProtocolException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
                return null;
        }
        
        protected void onPostExecute(HttpResponse result) {
                try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(result.getEntity().getContent()));
                        //String first_line = reader.readLine();
                        //Toast.makeText(getApplicationContext(), first_line, Toast.LENGTH_LONG).show();    
	                    try{
	                    	String line = reader.readLine();
	                    	String allText = "";
	                    	while(line !=null){
	                    		Log.i(TAG, line);
	                    		allText += line;
	                    		line = reader.readLine();
	                    		
	                        }
	                    	TextView textView = (TextView)findViewById(R.id.outputTextView);
                    		textView.setText(allText);
	                    } catch (IOException e){
	                    	
	                    }
                        
                } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
        }
}
    
}
