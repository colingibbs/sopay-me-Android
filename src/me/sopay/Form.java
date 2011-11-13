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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.inputmethod.InputMethodManager;
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
	private static final String site = "sopay-me.appspot.com";
	private static final String fullsite = "https://" + site + "/rpc";
	private static final String TAG = "SoPayMeActivity-AppInfo";
	private Account account;
	int numPeople = 1;
	
	//store all of the EditText view IDs in a 2D array
	private int[][] editTextViewIds = {
			{R.id.title, R.id.details,},
			{R.id.enterName1, R.id.enterName2, R.id.enterName3, R.id.enterName4,},
			{R.id.enterAmount1, R.id.enterAmount2, R.id.enterAmount3, R.id.enterAmount4,},
	};
	//ids for each TableLayout that holds the email and amount fields
	private int[] labelViewIds = {R.id.person1, R.id.person2, R.id.person3, R.id.person4};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.form);
            
            //hide all of the input fields except the first group
            for(int i=1; i<labelViewIds.length; i++){
        		View temp = findViewById(labelViewIds[i]);
        		temp.setVisibility(View.GONE);
            }
            
            //set all of the autocompleters            
            ContentResolver content = getContentResolver();
            Cursor cursor = content.query(Email.CONTENT_URI, CONTACT_PROJECTION, null, null, null);
            ContactListAdapter adapter = new ContactListAdapter(this, cursor);

            AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.enterName1);
            textView.setAdapter(adapter);
            textView = (AutoCompleteTextView) findViewById(R.id.enterName2);
            textView.setAdapter(adapter);
            textView = (AutoCompleteTextView) findViewById(R.id.enterName3);
            textView.setAdapter(adapter);
            textView = (AutoCompleteTextView) findViewById(R.id.enterName4);
            textView.setAdapter(adapter);
            
            final Button submitButton = (Button) findViewById(R.id.submit);
            submitButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    // build the JSON object for the order
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
                    	
                    	//add the emails that the user entered  (1=emails)
                    	for(int i=0; i<editTextViewIds[1].length; i++){
                    		EditText email = (EditText) findViewById(editTextViewIds[1][i]);
                    		String e = email.getText().toString();
                    		if(e.length() > 0)
                    			order.accumulate("emails", e);
                    	}                    	
                    	
                    	//add the amounts that the user entered (2=amounts)
                    	for(int i=0; i<editTextViewIds[2].length; i++){
                    		EditText amount = (EditText) findViewById(editTextViewIds[2][i]);
                    		String a = amount.getText().toString();
                    		if(a.length() > 0)
                    			order.accumulate("amounts", a);
                    	}    
                    	
                    	//hide the keyboard
                    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    	imm.hideSoftInputFromWindow(findViewById(R.id.form).getWindowToken(), 0);
                    	
                    	Log.i(TAG, "SENDING ORDER: " + order.toString());
                        new SubmitOrder(Form.this, fullsite, http_client).execute(order);                    	
                	}
                	catch(Exception e){
                		e.printStackTrace();
                	}

                }
            });
            
            final Button clearButton = (Button) findViewById(R.id.clear);
            clearButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                	ClearForm();
                }
            });
            
            final Button addButton = (Button) findViewById(R.id.add);
            addButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                	if(numPeople<4){
                		View temp = findViewById(labelViewIds[numPeople]);
                		temp.setVisibility(View.VISIBLE);
                		//setContentView(R.layout.form);
                		numPeople++;
                	}else{
                		String text = "You can only include 4 people in the order.";
                		Toast.makeText(Form.this, text, Toast.LENGTH_LONG).show();
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
            
            setTitle("So Pay Me - " + account.name);
            
            accountManager.getAuthToken(account, "ah", false, new GetAuthTokenCallback(), null);
    }
    
    protected void ClearForm(){
    	for(int i=0; i<editTextViewIds.length; i++){
    		for(int j=0; j<editTextViewIds[i].length; j++){
    			EditText temp = (EditText) findViewById(editTextViewIds[i][j]);
        		temp.setText("");
    		}
    	}
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
