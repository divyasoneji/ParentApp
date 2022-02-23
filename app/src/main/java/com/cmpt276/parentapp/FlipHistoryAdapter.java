package com.cmpt276.parentapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.cmpt276.model.Child;
import com.cmpt276.model.Coin;
import com.cmpt276.parentapp.serializer.LocalDateTimeAdapter;

import java.util.ArrayList;

/**
 * Adapter for recyclerview in viewing history of flips.
 */
public class FlipHistoryAdapter extends RecyclerView.Adapter<FlipHistoryAdapter.ViewHolder> {

	//taken more or less from android's recyclerview guide
	//https://developer.android.com/guide/topics/ui/layout/recyclerview

	/**
	 * Custom views
	 * */
	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView textViewFlipResult;
		private final TextView textViewChildPicked;
		private final TextView textViewTime;
		private final ImageView imageViewAcceptedFlip;
		private final ImageView imageViewChildIcon;

		public ViewHolder(View view) {
			super(view);
			textViewFlipResult = view.findViewById(R.id.textViewFlipResult);
			textViewChildPicked = view.findViewById(R.id.textViewChildPicked);
			textViewTime = view.findViewById(R.id.textViewFlipTime);
			imageViewAcceptedFlip = view.findViewById(R.id.imageViewAcceptedFlip);
			imageViewChildIcon = view.findViewById(R.id.imageViewCoinHistoryChild);
		}

		public TextView getTextViewFlipResult() {
			return textViewFlipResult;
		}

		public TextView getTextViewChildPicked() {
			return textViewChildPicked;
		}

		public TextView getTextViewTime() {
			return textViewTime;
		}

		public ImageView getImageViewResult() {
			return imageViewAcceptedFlip;
		}

		public ImageView getImageViewChildIcon() {
			return imageViewChildIcon;
		}
	}

	private Context context;
	private ArrayList<Coin> flips;

	public FlipHistoryAdapter(Context context, ArrayList<Coin> flips) {
		this.context = context;
		this.flips = flips;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.flip_history_list_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		Coin coin = flips.get(position);
		Child child = coin.getChild();
		String flipResult;
		switch (coin.getFlipResult()) {
			case Coin.HEADS:
				flipResult = context.getString(R.string.heads);
				break;
			case Coin.TAILS:
				flipResult = context.getString(R.string.tails);
				break;
			default:
				throw new IllegalStateException("Cannot have coin flip that has result neither heads nor tails.");
		}

		String flipResultString = context.getString(R.string.coin_flip_result, flipResult);
		holder.getTextViewFlipResult().setText(flipResultString);

		String childPickedString = context.getString(R.string.coin_flip_picked, child.getName());
		holder.getTextViewChildPicked().setText(childPickedString);

		String timeString = context.getString(R.string.coin_flip_time, LocalDateTimeAdapter.getTimeFormatted(coin.getTime()));
		holder.getTextViewTime().setText(timeString);

		if (coin.hasNoChoice()) {
			Drawable heart = AppCompatResources.getDrawable(context, R.drawable.ic_round_favorite_24);
			holder.getImageViewResult().setImageDrawable(heart);
		}
		else {
			if (coin.getFlipChoice() == coin.getFlipResult()) {
				Drawable checkmark = AppCompatResources.getDrawable(context, R.drawable.ic_round_check_circle_24);
				holder.getImageViewResult().setImageDrawable(checkmark);
			}
			else {
				Drawable cross = AppCompatResources.getDrawable(context, R.drawable.ic_round_cancel_24);
				holder.getImageViewResult().setImageDrawable(cross);
			}
		}

		if (child.getImageBitmap() == null) {
			holder.getImageViewChildIcon().setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.default_image));
		}
		else {
			holder.getImageViewChildIcon().setImageBitmap(child.getImageBitmap());
		}
	}

	@Override
	public int getItemCount() {
		return flips.size();
	}
}
