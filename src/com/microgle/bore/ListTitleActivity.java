package com.microgle.bore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ListTitleActivity extends ListActivity {

	private String assetsDir = "mhbrj1";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.titlelist);
		intent = this.getIntent();
		Bundle bunde = intent.getExtras();
		assetsDir = bunde.getString("assetsDir");
		fill(bunde.getStringArray("titlelist"));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Bundle bundle = new Bundle();
		bundle.putInt("position", position);
		bundle.putString("assetsDir", assetsDir);
		intent.putExtras(bundle);
    	//startActivityForResult(intent, 0);
    	ListTitleActivity.this.setResult(RESULT_OK, intent);  
        //关闭当前activity  
    	ListTitleActivity.this.finish(); 
	}
	
	private void fill(String[] titlelist) {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titlelist);
		setListAdapter(adapter);
	}
	private Intent intent;  
}
