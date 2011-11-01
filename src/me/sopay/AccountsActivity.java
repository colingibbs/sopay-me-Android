package me.sopay;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AccountsActivity extends ListActivity {	
	
    private static final String TAG = "SoPayMe-AccountsActivity";

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.i(TAG, "onCreate");
    	super.onCreate(savedInstanceState);
    	
    	Log.i(TAG, "opening account manager");
    	
    	AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType("com.google");
        this.setListAdapter(new ArrayAdapter(this, R.layout.list_item, accounts));
    }

    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
            Account account = (Account)getListView().getItemAtPosition(position);
            Preferences.setAccount(this, account);
            setResult(RESULT_OK);
            finish();
    }
}
