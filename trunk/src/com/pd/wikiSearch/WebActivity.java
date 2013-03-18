package com.pd.wikiSearch;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

public class WebActivity extends Activity {
	
	public static final String EXTRA_URL = "url";
	
	private WebView webView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web);
		
		// obtain UI object
		this.webView = (WebView) findViewById(R.id.web_page);
		
		// setting up webView properties
		WebSettings webSettings = this.webView.getSettings();
		webSettings.setLoadsImagesAutomatically(false);
		//webSettings.setJavaScriptEnabled(true);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		try {
			this.webView.loadUrl(getIntent().getStringExtra(WebActivity.EXTRA_URL));
		} catch (Exception e) {
			Toast.makeText(this, getString(R.string.unableToLoadPage), Toast.LENGTH_LONG).show();
			finish();
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		onResume();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.web_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
	        case R.id.web_menu_openInBrowser:
	        	startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(this.webView.getUrl())));
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
}
