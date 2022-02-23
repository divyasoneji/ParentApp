package com.cmpt276.parentapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.cmpt276.model.Child;
import com.cmpt276.parentapp.serializer.ImageOperations;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

/**
 * Activity for adding, editing, and deleting saved children
 */
public class ChildrenActivity extends AppCompatActivity {

	private Options options;
	private ImageHandler imageHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_children);
		options = Options.getInstance();
		imageHandler = new ImageHandler();

		setUpEmptyMessage();
		setUpAddBtn();
		populateList();
		setUpBackBtn();
		listItemClick();
	}

	/**
	 * Handles children's images by converting bitmap into Base-64 strings
	 * */
	public class ImageHandler {

		private static final int SELECT_FROM_GALLERY = 1;
		private static final int TAKE_NEW_PHOTO = 2;

		private String encodedResult;
		private int photoActivityCode; // 1 for select from gallery, 2 for taking new photo
		private ImageView output;     //pass in preview image from dialogs
		private final ActivityResultLauncher<Intent> openPhotoActivity;

		public ImageHandler() {
			//ActivityResultLaunchers can only be initialized in OnCreate, hence
			//the weird way to pass things into the ActivityResultLauncher
			openPhotoActivity = getPhotoActivity();
		}

		private void selectFromPhotos(ImageView showImage) {
			photoActivityCode = SELECT_FROM_GALLERY;
			output = showImage;
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			openPhotoActivity.launch(intent);
		}

		private void takePhoto(ImageView showImage) {
			photoActivityCode = TAKE_NEW_PHOTO;
			output = showImage;
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			openPhotoActivity.launch(intent);
		}

		private ActivityResultLauncher<Intent> getPhotoActivity() {
			return registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == Activity.RESULT_OK) {
							Intent data = result.getData();
							switch (photoActivityCode) {
								case SELECT_FROM_GALLERY:
									Uri selectedImageUri = data.getData();
									if (selectedImageUri != null) {
										try {
											Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
											encodedResult = ImageOperations.encodeBitmap(bitmap);
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
									break;
								case TAKE_NEW_PHOTO:
									Bitmap bitmap = (Bitmap) data.getExtras().get("data");
									encodedResult = ImageOperations.encodeBitmap(bitmap);
									break;
								default:
									throw new IllegalStateException("Invalid photo activity code.");
							}
							output.setImageBitmap(ImageOperations.decodeBitmap(encodedResult));
						}
					});
		}
	}

	private void setUpEmptyMessage() {
		TextView emptyChildMessage = findViewById(R.id.empty_child_message);
		if (options.getChildList(this).size() == 0) {
			emptyChildMessage.setVisibility(View.VISIBLE);
		}
		else {
			emptyChildMessage.setVisibility(View.GONE);
		}
	}

	public static Intent getIntent(Context context) {
		return new Intent(context, ChildrenActivity.class);
	}

	private void setUpBackBtn() {
		Button backBtn = findViewById(R.id.backBtn_children);
		backBtn.setText(R.string.back);
		backBtn.setOnClickListener((view) -> finish());
	}

	private void setUpAddBtn() {
		Button addBtn = findViewById(R.id.addBtn);
		addBtn.setText(R.string.add);

		addBtn.setOnClickListener((view) -> {
			AddChildDialog alert = new AddChildDialog();
			alert.showDialog(ChildrenActivity.this);
		});
	}

	//Populate list view with name and age of children
	private void populateList() {
		ArrayAdapter<Child> adapter = new ChildrenListViewAdapter();
		ListView childrenListView = findViewById(R.id.childrenListView);
		childrenListView.setAdapter(adapter);
		childrenListView.setDivider(null);
		childrenListView.setDividerHeight(16);
	}

	/**
	 * Custom adapter for children's names and photos in listview
	 * */
	private class ChildrenListViewAdapter extends ArrayAdapter<Child> {

		public ChildrenListViewAdapter() {
			super(ChildrenActivity.this, R.layout.children_view, options.getChildList(ChildrenActivity.this));
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View gamesView = convertView;
			if (gamesView == null) {
				gamesView = getLayoutInflater().inflate(R.layout.children_view, parent, false);
			}

			Child currentChild = options.getChildList(ChildrenActivity.this).get(position);

			ImageView childImage = gamesView.findViewById(R.id.children_name_list_image);
			if (currentChild.getEncodedImage() != null) {
				childImage.setImageBitmap(ImageOperations.decodeBitmap(currentChild.getEncodedImage()));
			}

			// Set up game ListView item
			TextView childName = gamesView.findViewById(R.id.child_name);
			childName.setText(currentChild.getName());
			return gamesView;
		}

	}

	//Click handling for children list view
	private void listItemClick() {
		if (options.getChildList(ChildrenActivity.this).size() == 0) {
			return;
		}
		ListView childrenListView = findViewById(R.id.childrenListView);
		childrenListView.setOnItemClickListener((adapterView, childClicked, index, position) -> {
			EditChildDialog alert = new EditChildDialog();
			alert.showDialog(ChildrenActivity.this, index);
		});
	}

	/**
	 * Custom dialog to add child
	 * */
	public class AddChildDialog {
		private boolean hasNewImage = false;

		public void showDialog(Activity activity) {
			final Dialog dialog = new Dialog(activity);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setCancelable(false);
			dialog.setContentView(R.layout.add_child_dialog);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

			EditText nameInput = dialog.findViewById(R.id.childNameEditText);

			//Image menu to select or take new image
			String[] optionItem = getResources().getStringArray(R.array.add_image_option);
			ArrayAdapter<String> adapter = new ArrayAdapter<>(ChildrenActivity.this, R.layout.pick_image_text_view, optionItem);

			ListView pickImage = dialog.findViewById(R.id.addImage);
			pickImage.setAdapter(adapter);
			pickImage.setVisibility(View.INVISIBLE);

			ImageView addChildImage = dialog.findViewById(R.id.child_image);
			addChildImage.setOnClickListener(v -> pickImage.setVisibility(View.VISIBLE));

			pickImage.setOnItemClickListener((parent, view, position, id) -> {
				switch (position) {
					case 0:
						// Select from gallery
						imageHandler.selectFromPhotos(addChildImage);
						pickImage.setVisibility(View.INVISIBLE);
						hasNewImage = true;
						break;

					case 1:
						// Take new photo
						imageHandler.takePhoto(addChildImage);
						pickImage.setVisibility(View.INVISIBLE);
						hasNewImage = true;
						break;

					case 2:
						pickImage.setVisibility(View.INVISIBLE);
						break;
				}
			});

			FloatingActionButton cancelFab = dialog.findViewById(R.id.cancelfab);
			cancelFab.setOnClickListener(getCancelFabListener(dialog));

			FloatingActionButton addFab = dialog.findViewById(R.id.addfab);
			addFab.setOnClickListener(getAddFabListener(dialog, nameInput));

			dialog.show();
		}

		private View.OnClickListener getAddFabListener(Dialog dialog, EditText nameInput) {
			return (view) -> {
				if (nameInput.getText().toString().isEmpty()) {
					Toast.makeText(ChildrenActivity.this, R.string.error_validate_name, Toast.LENGTH_SHORT).show();
				}
				else {
					if (hasNewImage) {
						options.addChild(ChildrenActivity.this, nameInput.getText().toString(), imageHandler.encodedResult);
					}
					else {
						options.addChild(ChildrenActivity.this, nameInput.getText().toString());
					}

					populateList();
					listItemClick();
					setUpEmptyMessage();
					dialog.cancel();
				}
			};
		}

		private View.OnClickListener getCancelFabListener(Dialog dialog) {
			return (view) -> dialog.dismiss();
		}
	}

	/**
	 * Custom dialog to edit child
	 * */
	public class EditChildDialog {
		boolean hasNewImage = false;

		public void showDialog(Activity activity, int index) {
			final Dialog dialog = new Dialog(activity);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setCancelable(false);
			dialog.setContentView(R.layout.edit_child_dialog);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

			EditText nameInput = dialog.findViewById(R.id.childNameEditText2);
			nameInput.setText(options.getChildList(ChildrenActivity.this).get(index).getName());

			//Similar to add child dialog
			String[] optionItem = getResources().getStringArray(R.array.add_image_option);
			ArrayAdapter<String> adapter = new ArrayAdapter<>(ChildrenActivity.this, R.layout.pick_image_text_view, optionItem);

			ListView pickImage = dialog.findViewById(R.id.edit_image);
			pickImage.setAdapter(adapter);
			pickImage.setVisibility(View.INVISIBLE);

			Child currentChild = options.getChildList(ChildrenActivity.this).get(index);

			ImageView editChildImage = dialog.findViewById(R.id.child_image_edit);

			if (currentChild.getEncodedImage() != null) {
				editChildImage.setImageBitmap(ImageOperations.decodeBitmap(currentChild.getEncodedImage()));
			}

			editChildImage.setOnClickListener(v -> {
				pickImage.setVisibility(View.VISIBLE);
			});

			pickImage.setOnItemClickListener((parent, view, position, id) -> {
				switch (position) {
					case 0:
						// Select from gallery
						imageHandler.selectFromPhotos(editChildImage);
						pickImage.setVisibility(View.INVISIBLE);
						hasNewImage = true;
						break;

					case 1:
						// Take new photo
						imageHandler.takePhoto(editChildImage);
						pickImage.setVisibility(View.INVISIBLE);
						hasNewImage = true;
						break;

					case 2:
						pickImage.setVisibility(View.INVISIBLE);
						break;
				}

				nameInput.setText(options.getChildList(ChildrenActivity.this).get(index).getName());

			});

			FloatingActionButton cancelFab = dialog.findViewById(R.id.cancelfab2);
			cancelFab.setOnClickListener(getCancelFabListener(dialog));

			FloatingActionButton addFab = dialog.findViewById(R.id.addfab2);
			addFab.setOnClickListener(getAddFabListener(dialog, nameInput, index));

			FloatingActionButton deleteFab = dialog.findViewById(R.id.deletefab2);
			deleteFab.setOnClickListener(getDeleteFabListener(dialog, index));

			dialog.show();
		}

		private View.OnClickListener getCancelFabListener(Dialog dialog) {
			return (view) -> dialog.dismiss();
		}

		private View.OnClickListener getAddFabListener(Dialog dialog, EditText nameInput, int index) {
			return (view) -> {
				if (nameInput.getText().toString().isEmpty()) {
					Toast.makeText(ChildrenActivity.this, R.string.error_validate_name, Toast.LENGTH_SHORT).show();
				}
				else {
					if (hasNewImage) {
						options.editChildImage(ChildrenActivity.this, index, imageHandler.encodedResult);
					}
					options.editChildName(ChildrenActivity.this, index, nameInput.getText().toString());

					populateList();
					listItemClick();
					setUpEmptyMessage();
					dialog.cancel();
				}
			};
		}

		private View.OnClickListener getDeleteFabListener(Dialog dialog, int index) {
			return (view) -> {
				options.removeChild(ChildrenActivity.this, index);
				populateList();
				listItemClick();
				setUpEmptyMessage();
				dialog.cancel();
			};
		}
	}

}