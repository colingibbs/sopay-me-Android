package me.sopay;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
/*
 * This class is used to submit the sopayme orders to the server.  It's called from
 * Form.java.  It also displays the progress and result dialogs.
 */

public class SubmitOrder extends AsyncTask<JSONObject, Void, HttpResponse> {
	ProgressDialog dialog;
	String site;
	Context formContext;
	DefaultHttpClient http_client;
	 
	public SubmitOrder(Context context, String site, DefaultHttpClient http_client){
		this.site=site;
		this.http_client = http_client;
		this.dialog = new ProgressDialog(context);
		this.formContext = context;
		
		dialog.setMessage("Submitting order. Please wait..");
		dialog.show();
	}
	
	@Override
    protected HttpResponse doInBackground(JSONObject... order) {    		
			try {
				
				HttpPost http_post = new HttpPost(site);
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
            	dialog.dismiss();
                int status = result.getStatusLine().getStatusCode();
                String message;
                
                switch(status){
                case 200://success
                	message = "Order submitted successfully!";
                	break;
            	default: //all failures until I start setting the error on the server appropriately
            		message = "Oops...looks like something went wrong.  " +
            				"Please try submitting your order again.";
                }
                
                //show the summary dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(formContext);
                builder.setMessage(message)
                       .setCancelable(true)
                       .setNeutralButton("Done", new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                           }
                       }).show();                        
            } catch (IllegalStateException e) {
                    e.printStackTrace();
            }
    }
}