package com.pd.wikiSearch;

import java.util.StringTokenizer;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;

public class SuggestTask extends AsyncTask<String, String, String[]> {

	@Override
	protected String[] doInBackground(String... params) {
		try {
			StringTokenizer tokenizer = new StringTokenizer(EntityUtils.toString(
					new DefaultHttpClient()
						.execute(
								new HttpGet(
										"http://" + params[0] 
										+ "/w/api.php?action=opensearch&search=" 
										+ params[1] + "&limit=" + params[2]
								)
						).getEntity()), ",");
			tokenizer.nextToken();
			String[] suggests = new String[tokenizer.countTokens()];
			int i = 0;
			while (tokenizer.hasMoreTokens()) {
				suggests[i++] = tokenizer.nextToken()
					.replaceAll("\"", "")
					.replaceAll("\\]", "")
					.replaceAll("\\[", ""); // TODO one regex
			}
			return suggests;
		} catch (Exception e) {}
		return null;
	}

}
