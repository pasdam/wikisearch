package com.miraiCreative.wikiSearch;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends Activity {

	private static ArrayAdapter<ResultItem> resultsAdapter;
	
	private AsyncTask<String, String, String[]> task;

	private AutoCompleteTextView searchBoxTxt;
	private ImageButton searchBtn;
	private Button moreBtn;
	private ListView resultLst;

	private String domain;

	private String language;

	private String numberOfSuggest;
	private boolean suggests;
	private String numberOfResults;

	private boolean loadMore;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.search);
		
		// obtain UI object
		this.searchBoxTxt = (AutoCompleteTextView) findViewById(R.id.search_searchboxTxt);
		this.searchBtn = (ImageButton) findViewById(R.id.search_searchBtn);
		this.moreBtn = (Button) findViewById(R.id.search_moreBtn);
		this.resultLst = (ListView) findViewById(R.id.search_resultLst);
		
		// add event handler
		this.searchBoxTxt.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start,
					int before, int count) {
				if (SearchActivity.this.suggests) {
					if (SearchActivity.this.task != null) {
						SearchActivity.this.task.cancel(true);
					}
					if (count != 0) {
						SearchActivity.this.task = (new SuggestTask())
								.execute(SearchActivity.this.language + "."
										+ SearchActivity.this.domain,
										s.toString(),
										SearchActivity.this.numberOfSuggest);
						try {
							String[] suggests = SearchActivity.this.task
									.get();
							if (suggests != null) {
								ArrayAdapter<String> adapter = new ArrayAdapter<String>(
										SearchActivity.this,
										R.layout.search_suggest_item,
										suggests);
								SearchActivity.this.searchBoxTxt
										.setAdapter(adapter);
							}
						} catch (Exception e) {}
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start,
					int count, int after) {}

			@Override
			public void afterTextChanged(Editable s) {}
		});
		this.searchBoxTxt.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				SearchActivity.this.launchWebView(((TextView) arg1)
						.getText().toString());
			}
		});
		this.searchBoxTxt.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					return SearchActivity.this.searchBtn.performClick();
				}
				return false;
			}
		});
		this.searchBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					SearchActivity.resultsAdapter.clear();
					SearchActivity.this.addResultToList(
							EntityUtils.toString(
									new DefaultHttpClient()
										.execute(
												new HttpGet("http://" + SearchActivity.this.language 
														+ "." + SearchActivity.this.domain 
														+ "/w/api.php?action=query&list=search&srprop=snippet&format=xml&srsearch="
														+ SearchActivity.this.searchBoxTxt.getText().toString() 
														+ "&srlimit=" + SearchActivity.this.numberOfResults
														+ "sroffset" + SearchActivity.this.resultLst.getCount()))
													.getEntity()));
				} catch (Exception e) {
//					e.printStackTrace();
					Toast.makeText(SearchActivity.this, R.string.connectionError, Toast.LENGTH_LONG).show();
				}
			}
		});
		this.resultLst.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				SearchActivity.this.launchWebView(((TextView)arg1.findViewById(R.id.search_results_item_titleTxt)).getText().toString());
			}
		});
		this.resultLst.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if(SearchActivity.this.loadMore && ((firstVisibleItem + visibleItemCount) == totalItemCount) && !(SearchActivity.this.searchBoxTxt.getText().toString().equals(""))){
					try {
						SearchActivity.this.addResultToList(
							EntityUtils.toString(
									new DefaultHttpClient()
										.execute(
												new HttpGet("http://" + SearchActivity.this.language 
														+ "." + SearchActivity.this.domain 
														+ "/w/api.php?action=query&list=search&srprop=snippet&format=xml&srsearch="
														+ SearchActivity.this.searchBoxTxt.getText().toString() 
														+ "&srlimit=" + SearchActivity.this.numberOfResults
														+ "&sroffset=" + totalItemCount))
													.getEntity()));
					} catch (Exception e) {
						Toast.makeText(SearchActivity.this, SearchActivity.this.getString(R.string.unableToLoadMoreResults), Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		this.moreBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SearchActivity.this.startActivity(new Intent(SearchActivity.this, ChooseServiceActivity.class));
			}
		});
		
		// setting up ListView adapter
		SearchActivity.resultsAdapter = new ResultListAdapter(this, R.layout.search_results_list_item);
		this.resultLst.setAdapter(SearchActivity.resultsAdapter);
		
		// register for context menu
        registerForContextMenu(this.resultLst);
        
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		this.numberOfSuggest = defaultPrefs.getString(getString(R.string.PREFS_SUGGESTS_NUMBER), getString(R.string.PREFS_NUMBER_OF_SUGGESTS_DEFAULT));
		this.numberOfResults = defaultPrefs.getString(getString(R.string.PREFS_RESULTS_NUMBER), getString(R.string.PREFS_NUMBER_OF_RESULTS_DEFAULT));
		this.suggests = defaultPrefs.getBoolean(getString(R.string.PREFS_SUGGESTS_ENABLED), true);
		this.loadMore = defaultPrefs.getBoolean(getString(R.string.PREFS_RESULTS_LOAD_MORE), false);
		
		SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.PREFS), MODE_PRIVATE);
		
		String service = sharedPrefs.getString(getString(R.string.PREFS_LAST_SERVICE), Utils.SERVICES[Utils.DEFAULT_SERVICE]);
		this.domain = service + ".org";
		this.language = sharedPrefs.getString(getString(R.string.PREFS_LAST_WIKI), Utils.WIKI[Utils.DEFAULT_LANG_WIKI]);
		
		if (this.suggests == false) {
			String[] suggests = new String[] {};
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					SearchActivity.this, R.layout.search_suggest_item, suggests);
			SearchActivity.this.searchBoxTxt.setAdapter(adapter);
		}
		
		this.setTitle(service);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
	        case R.id.search_preferences:
	        	startActivity(new Intent(this, Preferences.class));
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      getMenuInflater().inflate(R.menu.search_resutls_context_menu, menu);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case R.id.search_results_context_menu_open_in_browser:
      	startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(this.language + "." + this.domain + "/wiki/" + ((TextView)this.resultLst.findViewById(R.id.search_results_item_titleTxt)).getText().toString().replaceAll(" ", "_"))));
        return true;
	default:
        return super.onContextItemSelected(item);
      }
    }
	
	private void addResultToList(String xmlResults) throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		
		InputSource inputSrc = new InputSource();
		inputSrc.setCharacterStream(new StringReader(xmlResults));
		
		Document doc = db.parse(inputSrc);
		
		NodeList nodeList = doc.getElementsByTagName("p");
		
		NamedNodeMap attributes;
		ResultItem currentItem;
		for (int i = 0; i < nodeList.getLength(); i++) {
			attributes = nodeList.item(i).getAttributes();
			currentItem = new ResultItem();
			currentItem.title = attributes.getNamedItem("title").getNodeValue();
			currentItem.description = attributes.getNamedItem("snippet").getNodeValue();
			SearchActivity.resultsAdapter.add(currentItem);
		}
	}
	
	private void launchWebView(String pageTitle){
		Intent intent = new Intent(this, WebActivity.class);
		intent.putExtra(WebActivity.EXTRA_URL, "http://" + this.language + ".m." + this.domain + "/wiki/" + pageTitle.replaceAll(" ", "_"));
		startActivity(intent);
	}
}
