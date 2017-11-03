package com.fisincorporated.exercisetracker.ui.stats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fisincorporated.exercisetracker.R;


// see http://www.vogella.com/tutorials/AndroidListView/article.html
public class StatsArrayAdapter extends ArrayAdapter<String[]> {
	private final Context context;
	private String[][] values;

	private class ViewHolder {
		public TextView label;
		public TextView stats;
	}

	public StatsArrayAdapter(Context context, String[][] values) {
		super(context, R.layout.activity_stats_detail, values);
		this.context = context;
		this.values = values;
	}
	
	public void clear(){
		this.values = null;
	}

	public int getCount(){
		if (values == null ) {
			return 0;
		}
		else return values.length;
		
	}
	
	public void resetValues(String[][] values){
		this.values = values;
		this.notifyDataSetChanged();
	}

	@Override
	// see http://www.vogella.com/tutorials/AndroidListView/article.html for
	// optimization of inflate/findViewById
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			StatsArrayAdapter.ViewHolder viewHolder = new ViewHolder();
			rowView = inflater.inflate(R.layout.activity_stats_detail, parent,
					false);
			viewHolder.label = (TextView) rowView
					.findViewById(R.id.activity_stats_detail_tvLabel);
			viewHolder.stats = (TextView) rowView
					.findViewById(R.id.activity_stats_detail_tvStats);
			rowView.setTag(viewHolder);
		}
		StatsArrayAdapter.ViewHolder holder = (StatsArrayAdapter.ViewHolder) rowView.getTag();
		holder.label.setText(values[position][0]);
		holder.stats.setText(values[position][1]);
		return rowView;
	}
}