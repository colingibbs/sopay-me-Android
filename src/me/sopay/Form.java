package me.sopay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

public class Form extends Activity {

	DefaultHttpClient http_client = new DefaultHttpClient();
	private static final String site = "cgibbs-test.sopay-me.appspot.com";
	private static final String fullsite = "https://" + site + "/rpc";
	private static final String TAG = "SoPayMeActivity-AppInfo";
	private Account account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.form);
            
            ContentResolver content = getContentResolver();
            Cursor cursor = content.query(Email.CONTENT_URI, CONTACT_PROJECTION, null, null, null);
            
            ContactListAdapter adapter = new ContactListAdapter(this, cursor);

            //set all of the autocompleters
            AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.enterName1);
            textView.setAdapter(adapter);
            textView = (AutoCompleteTextView) findViewById(R.id.enterName2);
            textView.setAdapter(adapter);
            textView = (AutoCompleteTextView) findViewById(R.id.enterName3);
            textView.setAdapter(adapter);
            textView = (AutoCompleteTextView) findViewById(R.id.enterName4);
            textView.setAdapter(adapter);
            
            final Button button = (Button) findViewById(R.id.button);
            button.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    // submit the order
                	JSONObject order = new JSONObject();;
                	
                	try{                		
                		//specify that it's an order request
                		order.put("action", "Submit");
                		
                		//add the title that the user entered
                    	EditText title = (EditText) findViewById(R.id.title);
                    	order.put("title", title.getText());
                    	
                    	//add the details that the user entered
                    	EditText details = (EditText) findViewById(R.id.details);
                    	order.put("details", details.getText());
                    	
                    	//add the emails that the user entered
                    	EditText email1 = (EditText) findViewById(R.id.enterName1);
                    	EditText email2 = (EditText) findViewById(R.id.enterName2);
                    	EditText email3 = (EditText) findViewById(R.id.enterName3);
                    	EditText email4 = (EditText) findViewById(R.id.enterName4);
                    	
                    	String[] rawEmails = {email1.getText().toString(), email2.getText().toString(), 
                    			email3.getText().toString(), email4.getText().toString()};
                    	for(String e : rawEmails){
                    		if(e.length() > 0)
                    			order.accumulate("emails", e);
                    	}                    	
                    	
                    	//add the amounts that the user entered
                    	EditText amount1 = (EditText) findViewById(R.id.enterAmount1);
                    	EditText amount2 = (EditText) findViewById(R.id.enterAmount2);
                    	EditText amount3 = (EditText) findViewById(R.id.enterAmount3);
                    	EditText amount4 = (EditText) findViewById(R.id.enterAmount4);
                    	
                    	String[] rawAmounts = {amount1.getText().toString(), amount2.getText().toString(), 
                    			amount3.getText().toString(), amount4.getText().toString()};
                    	for(String a : rawAmounts){
                    		if(a.length() > 0)
                    			order.accumulate("amounts", a);
                    	}                   	
                        
                    	Log.i(TAG, "SENDING ORDER: " + order.toString());
                        new submitOrder().execute(order);
                        Toast.makeText(Form.this, "Submitting order...", Toast.LENGTH_SHORT).show();
                    	
                	}
                	catch(Exception e){
                		e.printStackTrace();
                	}

                }
            });
    }    
    
    
    @Override
    protected void onResume() {
            super.onResume();
            Intent intent = getIntent();
            AccountManager accountManager = AccountManager.get(getApplicationContext());
            account = (Account)intent.getExtras().get("account");
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
	    	//don't do anything after getting the cookie
	    }

    }

    private class submitOrder extends AsyncTask<JSONObject, Void, HttpResponse> {
        
    	@Override
        protected HttpResponse doInBackground(JSONObject... order) {
                
    			try {
                        HttpPost http_post = new HttpPost(fullsite);

                        StringEntity s = new StringEntity(order[0].toString());
                        s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                        HttpEntity entity = s;
                        http_post.setEntity(entity);
                        
                        
                        return http_client.execute(http_post);
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
	                    	
	                    } catch (IOException e){
	                    	e.printStackTrace();
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
    
    public static class ContactListAdapter extends CursorAdapter implements Filterable {
        public ContactListAdapter(Context context, Cursor c) {
            super(context, c);
            mContent = context.getContentResolver();
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final LayoutInflater inflater = LayoutInflater.from(context);
            final TextView view = (TextView) inflater.inflate(
                    android.R.layout.simple_dropdown_item_1line, parent, false);
            view.setText(cursor.getString(COLUMN_DISPLAY_NAME));
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView) view).setText(cursor.getString(COLUMN_DISPLAY_NAME));
        }

        @Override
        public String convertToString(Cursor cursor) {
            return cursor.getString(COLUMN_DISPLAY_NAME);
        }

        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            FilterQueryProvider filter = getFilterQueryProvider();
            if (filter != null) {
                return filter.runQuery(constraint);
            }

            Uri uri = Uri.withAppendedPath(
                    Email.CONTENT_FILTER_URI,
                    Uri.encode(constraint.toString()));
            return mContent.query(uri, CONTACT_PROJECTION, null, null, null);
        }

        private ContentResolver mContent;
    }

    public static final String[] CONTACT_PROJECTION = new String[] {
        Contacts._ID,
        Contacts.DISPLAY_NAME,
        Email.DATA
    };

    private static final int COLUMN_DISPLAY_NAME = 2;
    
}
