package me.sopay;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SoPayMeActivity extends Activity {
	/**
     * Tag for logging.
     */
    private static final String TAG = "SoPayMeActivity";
    
    protected Intent intent;
    protected Account account;
    protected final int ACCOUNT_CODE = 2;
    
 
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        account = Preferences.getAccount(getApplicationContext());
        if(account==null)
        {
        	Intent intent = new Intent(this, AccountsActivity.class);
	        startActivityForResult(intent, ACCOUNT_CODE);
        }else{
        	Intent intent = new Intent(this, Form.class);
            intent.putExtra("account", account);
            startActivity(intent);
        }        
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    	if (resultCode == RESULT_OK && requestCode == ACCOUNT_CODE) {
            startActivity(data);
            finish();
        }
    }
  
}
