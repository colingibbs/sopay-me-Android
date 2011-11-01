package me.sopay;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SoPayMeActivity extends ListActivity {
	/**
     * Tag for logging.
     */
    private static final String TAG = "SoPayMeActivity";
    
    protected Intent intent;
    protected Account account;
    
 
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.i(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        
        account = Preferences.getAccount(getApplicationContext());
        if(account==null)
        {
        	Log.i(TAG, "No account selected yet.  Launching account list");
        	
        	Intent intent = new Intent(this, AccountsActivity.class);
	        startActivityForResult(intent, 2);
        }else{
        	Intent intent = new Intent(this, Form.class);
            intent.putExtra("account", account);
            startActivity(intent);
        }
        	
        
        
    	        
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //1=success and 2=AccountsActivity
        if (resultCode == RESULT_OK && requestCode == 2) {
        	account = Preferences.getAccount(getApplicationContext());
        	Intent intent = new Intent(this, Form.class);
            intent.putExtra("account", account);
            startActivity(intent);
        }
    }
  
}
