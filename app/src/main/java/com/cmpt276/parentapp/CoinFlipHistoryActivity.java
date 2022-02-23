package com.cmpt276.parentapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cmpt276.model.Coin;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * Activity for viewing all previous results of coin flips, including who picked,
 * the result of the flip, and the time it happened.
 */
public class CoinFlipHistoryActivity extends AppCompatActivity {

	Options options;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_coin_flip_history);

		options = Options.getInstance();

		ArrayList<Coin> coinFlips = options.getFlipHistory(this);

		RecyclerView rv = findViewById(R.id.recyclerViewFlipHistory);
		rv.setLayoutManager(new LinearLayoutManager(this));
		FlipHistoryAdapter adapter = new FlipHistoryAdapter(this, coinFlips);
		rv.setAdapter(adapter);

		setUpBackBtn();
		setUpClearBtn();
		setUpEmptyMessage();
	}

	private void setUpBackBtn() {
		Button backBtn = findViewById(R.id.backBtn_history);
		backBtn.setText(R.string.back);
		backBtn.setOnClickListener((view) -> finish());
	}

	private void setUpClearBtn() {
		Button buttonClear = findViewById(R.id.buttonClearFlipHistory);
		buttonClear.setOnClickListener((view) -> {
			if (options.getFlipHistory(this).size() == 0) {
				Toast.makeText(this, R.string.error_no_history, Toast.LENGTH_SHORT).show();
			}
			else {
				ClearHistoryDialog alert = new ClearHistoryDialog();
				alert.showDialog(CoinFlipHistoryActivity.this);
			}
		});
	}
	public void setUpEmptyMessage(){
		TextView emptyMessage = findViewById(R.id.empty_coin_flip_history_message);
		if (options.getFlipHistory(this).isEmpty()){
			emptyMessage.setVisibility(View.VISIBLE);
		}
		else {
			emptyMessage.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * Custom dialog to clear history
	 * */
	public class ClearHistoryDialog {

		public void showDialog(Activity activity) {
			final Dialog dialog = new Dialog(activity);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setCancelable(false);
			dialog.setContentView(R.layout.clear_dialog);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

			FloatingActionButton cancelFab = dialog.findViewById(R.id.cancelFab);
			cancelFab.setOnClickListener(getCancelFabListener(dialog));

			FloatingActionButton addFab = dialog.findViewById(R.id.okFab);
			addFab.setOnClickListener(getAddFabListener());

			dialog.show();
		}

		private View.OnClickListener getCancelFabListener(Dialog dialog) {
			return (view) -> dialog.dismiss();
		}

		private View.OnClickListener getAddFabListener() {
			return (view) -> {
				options.clearCoinFlips(CoinFlipHistoryActivity.this);
				CoinFlipHistoryActivity.this.finish();
			};
		}
	}

	public static Intent getIntent(Context context) {
		return new Intent(context, CoinFlipHistoryActivity.class);
	}
}