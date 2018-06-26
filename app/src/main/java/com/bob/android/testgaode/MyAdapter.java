package com.bob.android.testgaode;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;

import java.util.ArrayList;

public class MyAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<PoiItem> arrayList;

	public MyAdapter(Context context, ArrayList<PoiItem> arrayList) {
		this.context = context;
		this.arrayList = arrayList;
	}

	@Override
	public int getCount() {

		return arrayList.size();
	}

	@Override
	public Object getItem(int position) {

		return arrayList.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(context, R.layout.item_listview, null);
			holder.item_name = (TextView) convertView
					.findViewById(R.id.item_name);
			holder.item_distance = (TextView) convertView
					.findViewById(R.id.item_distance);
			holder.item_location = (TextView) convertView
					.findViewById(R.id.item_location);
			holder.item_phone = (TextView) convertView
					.findViewById(R.id.item_phone);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.item_name.setText(String.valueOf(arrayList.get(position)));
		holder.item_distance.setText(String.valueOf(arrayList.get(position)
				.getDistance())+"米");
		holder.item_phone.setText("电话"+arrayList.get(position).getTel());
		holder.item_location.setText("纬度:"
				+ String.valueOf(arrayList.get(position).getLatLonPoint()
						.getLatitude())
				+ "纬度:"
				+ String.valueOf(arrayList.get(position).getLatLonPoint()
						.getLongitude()));

		return convertView;
	}

	static class ViewHolder {
		TextView item_name;         //名称
		TextView item_distance;     //距离
		TextView item_phone;        //电话
		TextView item_location;     //经纬度

	}

}
