package com.cmpt276.parentapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

//Display help screen activity
public class HelpActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);

		Button backBtn = (Button) findViewById(R.id.backBtn_help);
		backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});

		setUpLinks();
	}

	public static Intent getIntent(Context context){
		return new Intent(context, HelpActivity.class);
	}

	public void setUpLinks(){
		TextView link1 = findViewById(R.id.link_1);
		TextView link2 = findViewById(R.id.link_2);
		TextView link3 = findViewById(R.id.link_3);
		TextView link4 = findViewById(R.id.link_4);

		link1.setMovementMethod(LinkMovementMethod.getInstance());
		link2.setMovementMethod(LinkMovementMethod.getInstance());
		link3.setMovementMethod(LinkMovementMethod.getInstance());
		link4.setMovementMethod(LinkMovementMethod.getInstance());

	}
}