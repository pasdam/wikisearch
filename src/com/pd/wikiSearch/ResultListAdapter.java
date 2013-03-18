package com.pd.wikiSearch;

import java.util.ArrayList;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ResultListAdapter extends ArrayAdapter<ResultItem> {

	private ArrayList<ResultItem> items;
	private Context context;

	public ResultListAdapter(Context context, int textViewResourceId, ArrayList<ResultItem> items) {
		super(context, textViewResourceId, items);
        this.items = items;
        this.context = context;
	}

	public ResultListAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		this.items = new ArrayList<ResultItem>();
		this.context = context;
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.search_results_list_item, null);
            }
            ResultItem item = items.get(position);
            if (item != null) {
                    TextView titleTxt = (TextView) v.findViewById(R.id.search_results_item_titleTxt);
                    TextView descTxt = (TextView) v.findViewById(R.id.search_results_item_snippetTxt);
                    if (titleTxt != null) {
                          titleTxt.setText(item.title);                            }
                    if(descTxt != null){
                          descTxt.setText(Html.fromHtml(item.description));
                    }
            }
            return v;
    }
	
	@Override
	public void add(ResultItem object) {
		super.add(object);
		this.items.add(object);
	}
	
	@Override
	public void clear() {
		// TODO Auto-generated method stub
		super.clear();
		this.items.clear();
	}
}