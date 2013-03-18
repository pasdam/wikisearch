package com.miraiCreative.wikiSearch;

import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChooseServiceActivity extends Activity {
	
	private static final int DIALOG_LANG = 0;
	private static final int DIALOG_DOMAIN = 1;
	private static final int DIALOG_CUSTOM_LANG = 2;
	
	private Button langBtn;
	private Button domainBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.service_dialog);
		
		// obtain UI object
		this.langBtn = (Button) findViewById(R.id.service_langBtn);
		this.domainBtn = (Button) findViewById(R.id.service_domainBtn);
		
		// add event handler
		this.langBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ChooseServiceActivity.this.showDialog(ChooseServiceActivity.DIALOG_LANG);
			}
		});
		this.domainBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ChooseServiceActivity.this.showDialog(ChooseServiceActivity.DIALOG_DOMAIN);
			}
		});
		((Button) findViewById(R.id.service_closeBtn)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ChooseServiceActivity.this.finish();
			}
		});
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		
		SharedPreferences prefs = getSharedPreferences(getString(R.string.PREFS), MODE_PRIVATE);
		String lang = prefs.getString(getString(R.string.PREFS_LAST_LANG), Utils.LANGUAGES[Utils.DEFAULT_LANG_WIKI]);
		
		if (lang.equals(Utils.LANGUAGES[0])) {
			this.langBtn.setText(lang + ": " + prefs.getString(getString(R.string.PREFS_LAST_WIKI), Utils.WIKI[Utils.DEFAULT_LANG_WIKI]));
		} else {
			this.langBtn.setText(lang);
		}
		this.domainBtn.setText(prefs.getString(getString(R.string.PREFS_LAST_SERVICE), Utils.SERVICES[Utils.DEFAULT_SERVICE]));
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
	    switch (id) {
	    case DIALOG_LANG :
	    	AlertDialog.Builder langDialogBuilder = new AlertDialog.Builder(this);
	    	langDialogBuilder.setTitle(getString(R.string.chooseLang));
	    	langDialogBuilder.setItems(Utils.LANGUAGES, new DialogInterface.OnClickListener() {
	    	    public void onClick(DialogInterface dialog, int item) {
	    	    	if (item == 0) {
	    	    		ChooseServiceActivity.this.showDialog(DIALOG_CUSTOM_LANG);
						dialog.dismiss();
					} else {
						SharedPreferences.Editor editor = ChooseServiceActivity.this.getSharedPreferences(getString(R.string.PREFS), MODE_PRIVATE).edit();
		    	    	editor.putString(getString(R.string.PREFS_LAST_LANG), Utils.LANGUAGES[item]);
		    	    	editor.putString(getString(R.string.PREFS_LAST_WIKI), Utils.WIKI[item]);
		    	    	editor.commit();
		    	    	ChooseServiceActivity.this.onResume();
					}
	    	    }
	    	});
	    	return langDialogBuilder.create();
	    	
	    case DIALOG_DOMAIN :
	    	AlertDialog.Builder domainDialogBuilder = new AlertDialog.Builder(this);
	    	domainDialogBuilder.setTitle(getString(R.string.chooseService));
	    	domainDialogBuilder.setItems(getResources().getStringArray(R.array.SERVICE_NAMES), new DialogInterface.OnClickListener() {
	    	    public void onClick(DialogInterface dialog, int item) {
					SharedPreferences.Editor editor = ChooseServiceActivity.this.getSharedPreferences(getString(R.string.PREFS), MODE_PRIVATE).edit();
	    	    	editor.putString(getString(R.string.PREFS_LAST_SERVICE), Utils.SERVICES[item]);
	    	    	editor.commit();
	    	    	ChooseServiceActivity.this.onResume();
	    	    }
	    	});
	    	return domainDialogBuilder.create();
	    	
	    case DIALOG_CUSTOM_LANG :
	    	final Dialog customLangDialog = new Dialog(this);
	    	customLangDialog.setContentView(R.layout.custom_lang_dialog);
	    	((Button) customLangDialog.findViewById(R.id.customLangDialog_okBtn)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String text = ((EditText) customLangDialog.findViewById(R.id.customLangDialog_wikiTxt)).getText().toString();
					if (text.equals("")) {
						return;
					}
					SharedPreferences sharedPrefs = ChooseServiceActivity.this.getSharedPreferences(getString(R.string.PREFS), MODE_PRIVATE);
					SharedPreferences.Editor editor = sharedPrefs.edit();
					
					String service = text + "." + sharedPrefs.getString(getString(R.string.PREFS_LAST_SERVICE), Utils.SERVICES[Utils.DEFAULT_SERVICE]) + ".org";
					try {
						Log.i("WikiSearch", "Exec head req");
						new DefaultHttpClient().execute(new HttpHead("http://" + service));
					} catch (Exception e) {
						Log.i("WikiSearch", "Error");
						Toast.makeText(ChooseServiceActivity.this, ChooseServiceActivity.this.getString(R.string.notValidService) + ": " + service, Toast.LENGTH_LONG).show();
						return;
					}
					
					editor.putString(getString(R.string.PREFS_LAST_LANG), Utils.LANGUAGES[0]);
	    	    	editor.putString(getString(R.string.PREFS_LAST_WIKI), text);
	    	    	editor.commit();
	    	    	ChooseServiceActivity.this.onResume();
	    	    	customLangDialog.dismiss();
				}
			});
	    	return customLangDialog;
	    }
	    return null;
	}
}
