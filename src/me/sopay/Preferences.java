package me.sopay;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public final class Preferences {

	private static final String TAG = "SoPayMe-Preferences";

	
	public static Account getAccount(SharedPreferences preferences, Context context){
		String accountName = preferences.getString("account", null);
		
		if(accountName != null){
			
			AccountManager accountManager = AccountManager.get(context);
	        Account[] accounts = accountManager.getAccountsByType("com.google");
			
	        for(int i=0; i<accounts.length; i++){
	        	if(accountName.equals(accounts[i].name)){
	        		return accounts[i];
	        	}
	        }
	        
	        //need to handle this case better
	        Log.i(TAG, "Weird, the saved account is not on the device");
			return null; 
		}else{
			return null;
		}
	}
	
	public static Account getAccount(Context context){
		if(context==null){
			throw new NullPointerException();
		}
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return(getAccount(preferences, context));
	}
	
	public static void setAccount(Context context, Account account){
		if(context==null){
			throw new NullPointerException();
		}else if(account==null){
			throw new NullPointerException();
		}
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("account", account.name);
		editor.commit();
		
	}
	
}
