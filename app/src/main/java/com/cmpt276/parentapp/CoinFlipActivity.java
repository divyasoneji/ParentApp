package com.cmpt276.parentapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;

import com.cmpt276.model.Child;
import com.cmpt276.model.Coin;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * Activity for flipping coin
 */
public class CoinFlipActivity extends AppCompatActivity {

	private static final int NUMBER_OF_FLIPS = 7;
	private static Child EMPTY_CHILD; //meant to be final, but getString can't be used in static contexts

	Options options;
	private int coinChoiceIndex = Coin.HEADS;
	private ImageView coinImage;

	private int currentSide = R.drawable.heads;

	public static Intent getIntent(Context context) {
		return new Intent(context, CoinFlipActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_coin_flip);

		EMPTY_CHILD = new Child(getString(R.string.no_child), 0);

		setUpBackButton();

		options = Options.getInstance();
		coinImage = findViewById(R.id.coin);
		coinImage.setImageResource(R.drawable.heads);

		updateUI();

		setUpFlipButton();
		setUpChangeChildButton();
		setUpHistoryButton();

		RadioGroup group = findViewById(R.id.radioGroupFlipChoice);
		group.setOnCheckedChangeListener(getGroupOnCheckChangeListener());
	}

	private void setUpBackButton() {
		Button backBtn = findViewById(R.id.backBtn_coin);
		backBtn.setText(R.string.back);
		backBtn.setOnClickListener(view -> finish());
	}

	private void setUpFlipButton() {
		Button buttonFlipCoin = findViewById(R.id.buttonFlipCoin);
		buttonFlipCoin.setOnClickListener((view) -> {
			ArrayList<Child> children = options.getChildList(CoinFlipActivity.this);
			ArrayList<Integer> queue = options.getQueueOrder(CoinFlipActivity.this);
			boolean isNoChildFlipping = options.isNoChildFlipping(CoinFlipActivity.this);

			Coin coin;

			if (isNoChildFlipping || children.size() == 0) {
				coin = new Coin(EMPTY_CHILD, Coin.NO_CHOICE);
			}
			else {
				//if index is out of bounds because of children array resizing, default to the first child added
				int indexOfChildInFront = queue.get(0);
				if (indexOfChildInFront < 0 || indexOfChildInFront >= children.size()) {
					indexOfChildInFront = 0;
				}
				int flipChoice;
				switch (coinChoiceIndex) {
					case Coin.HEADS:
						flipChoice = Coin.HEADS;
						break;
					case Coin.TAILS:
						flipChoice = Coin.TAILS;
						break;
					default:
						throw new IllegalStateException("Cannot have selection that is neither heads nor tails.");
				}
				coin = new Coin(children.get(indexOfChildInFront), flipChoice);
			}

			MediaPlayer mp = MediaPlayer.create(this, R.raw.coinflip);
			flipCoinAnimationTrigger(coin.getFlipResult());
			mp.start();

			//cycles to the next child, looping back to the first if it reaches the end of the list
			if (!isNoChildFlipping && children.size() > 0) {
				options.advanceQueue(CoinFlipActivity.this);
			}
			options.addCoinFlip(CoinFlipActivity.this, coin);

			options.setNoChildFlipping(CoinFlipActivity.this, false);

			int result = coin.getFlipResult();
			TextView tv = findViewById(R.id.textViewShowResult);
			tv.setText("");
			Handler handler = new Handler();
			handler.postDelayed(() -> {
				switch (result) {
					case Coin.HEADS:
						tv.setText(R.string.heads);
						break;
					case Coin.TAILS:
						tv.setText(R.string.tails);
						break;
					default:
						assert false;
						break;
				}
				updateUI();
			}, 1100);

		});
	}

	private void setUpChangeChildButton() {
		Button buttonChangeChild = findViewById(R.id.buttonChangeChild);
		buttonChangeChild.setOnClickListener((view) -> {
			Dialog dialog = new Dialog(this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setCancelable(false);
			dialog.setContentView(R.layout.change_child_flip);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

			FloatingActionButton cancelFab = dialog.findViewById(R.id.cancelfab3);
			cancelFab.setOnClickListener(getCancelFabListener(dialog));

			Button noChildButton = dialog.findViewById(R.id.buttonNoChild);
			noChildButton.setOnClickListener(getNoChildListener(dialog));

			dialog.show();

			ArrayAdapter<Child> adapter = new ChildListAdapter();
			ListView listView = dialog.findViewById(R.id.listViewChildSelect);
			listView.setAdapter(adapter);
			listView.setDividerHeight(16);

			listView.setOnItemClickListener(getListViewClickListener(dialog));
		});
	}

	private void setUpHistoryButton() {
		Button buttonViewHistory = findViewById(R.id.buttonViewFlipHistory);
		buttonViewHistory.setOnClickListener((view) -> {
			Intent intent = CoinFlipHistoryActivity.getIntent(CoinFlipActivity.this);
			startActivity(intent);
		});
	}

	//Set up coin animation
	private void animateCoin(boolean stayTheSame) {
		CoinToss coinAnimation;

		if (currentSide == R.drawable.heads) {
			coinAnimation = new CoinToss(coinImage, R.drawable.heads, R.drawable.tails,
					0, 180, 0, 0, 0, 0);
		}
		else {
			coinAnimation = new CoinToss(coinImage, R.drawable.tails, R.drawable.heads,
					0, 180, 0, 0, 0, 0);
		}

		if (stayTheSame) {
			coinAnimation.setRepeatCount(NUMBER_OF_FLIPS); // value + 1 must be even so the side will stay the same
		}
		else {
			coinAnimation.setRepeatCount(NUMBER_OF_FLIPS + 1); // value + 1 must be odd so the side will not stay the same
		}

		coinAnimation.setDuration(100);
		coinAnimation.setInterpolator(new AccelerateInterpolator());
		coinImage.startAnimation(coinAnimation);
	}

	//Trigger coin animation
	public void flipCoinAnimationTrigger(int coinSide) {
		if (coinSide == Coin.HEADS) {
			boolean stayTheSame = (currentSide == R.drawable.heads);
			animateCoin(stayTheSame);
			currentSide = R.drawable.heads;
		}
		if (coinSide == Coin.TAILS) {
			boolean stayTheSame = (currentSide == R.drawable.tails);
			animateCoin(stayTheSame);
			currentSide = R.drawable.tails;
		}
	}

	private void updateUI() {
		TextView textViewChild = findViewById(R.id.textViewChild);
		LinearLayout flipChoiceLL = findViewById(R.id.linearLayout);
		CardView cardViewFlippingChild = findViewById(R.id.cardViewFlippingChild);
		ImageView imageViewFlippingChild = findViewById(R.id.imageViewFlippingChild);
		ArrayList<Child> children = options.getChildList(CoinFlipActivity.this);
		ArrayList<Integer> queue = options.getQueueOrder(CoinFlipActivity.this);
		boolean isNoChildFlipping = options.isNoChildFlipping(CoinFlipActivity.this);

		//if there's no children, essentially hide the text view.
		if (children.size() == 0) {
			textViewChild.setText(R.string.flip_a_coin);
			flipChoiceLL.setVisibility(View.INVISIBLE);
			cardViewFlippingChild.setVisibility(View.INVISIBLE);
		}
		else {
			if (isNoChildFlipping){
				textViewChild.setText(R.string.coin_flip_no_child_prompt);
				cardViewFlippingChild.setVisibility(View.INVISIBLE);
			}
			else {
				int indexOfChildInFront = queue.get(0);
				Child childInFront = children.get(indexOfChildInFront);
				textViewChild.setText(getString(R.string.coin_flip_current_child_prompt, childInFront.getName()));

				cardViewFlippingChild.setVisibility(View.VISIBLE);
				Bitmap childBitmap = childInFront.getImageBitmap();
				if (childBitmap != null){
					imageViewFlippingChild.setImageBitmap(childBitmap);
				}
				else {
					Drawable defaultImage = AppCompatResources.getDrawable(this, R.drawable.default_image);
					imageViewFlippingChild.setImageDrawable(defaultImage);
				}
			}

			flipChoiceLL.setVisibility(View.VISIBLE);
		}
	}

	private View.OnClickListener getCancelFabListener(Dialog dialog) {
		return (view) -> dialog.dismiss();
	}


	/**
	 * Custom adapter for child class
	 * */
	private class ChildListAdapter extends ArrayAdapter<Child> {

		ArrayList<Child> children = options.getChildList(CoinFlipActivity.this);
		ArrayList<Integer> flipOrder = options.getQueueOrder(CoinFlipActivity.this);

		public ChildListAdapter() {
			super(CoinFlipActivity.this, R.layout.child_name_view, options.getChildList(CoinFlipActivity.this));
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.child_name_view, parent, false);
			}

			Child currentChild = children.get(flipOrder.get(position));

			// set up game ListView item
			TextView childName = convertView.findViewById(R.id.change_child_name);
			childName.setText(currentChild.getName());

			ImageView childIcon = convertView.findViewById(R.id.imageViewChildSelect);
			if (currentChild.getImageBitmap() == null){
				childIcon.setImageDrawable(AppCompatResources.getDrawable(CoinFlipActivity.this, R.drawable.default_image));
			}
			else {
				childIcon.setImageBitmap(currentChild.getImageBitmap());
			}

			return convertView;
		}
	}

	private AdapterView.OnItemClickListener getListViewClickListener(Dialog dialog) {
		return (adapterView, view1, i, l) -> {
			options.moveToFrontOfQueue(CoinFlipActivity.this, i);
			options.setNoChildFlipping(CoinFlipActivity.this, false);
			updateUI();
			dialog.dismiss();
		};
	}

	private View.OnClickListener getNoChildListener(Dialog dialog) {
		return (view) -> {
			options.setNoChildFlipping(CoinFlipActivity.this, true);
			updateUI();
			dialog.dismiss();
		};
	}

	private RadioGroup.OnCheckedChangeListener getGroupOnCheckChangeListener() {
		return (RadioGroup radioGroup, int checkedId) -> {
			View radioButton = radioGroup.findViewById(checkedId);
			coinChoiceIndex = radioGroup.indexOfChild(radioButton);
		};
	}
}