package com.cmpt276.parentapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

import com.cmpt276.model.Child;
import com.cmpt276.model.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Activity for managing various tasks for children to do.
 * When a task is created, a child is randomly assigned to be the first to do it.
 * */
public class TaskActivity extends AppCompatActivity {

	public static Intent getIntent(Context context) {
		return new Intent(context, TaskActivity.class);
	}

	private Options options;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task);

		options = Options.getInstance();

		setUpAddTaskFAB();
		setUpBackBtn();
		populateTaskList();
	}

	private void setUpEmptyMessage() {
		TextView emptyTaskMessage = findViewById(R.id.empty_task_message);
		if (options.getTaskList(this).size() == 0) {
			emptyTaskMessage.setVisibility(View.VISIBLE);
		}
		else {
			emptyTaskMessage.setVisibility(View.GONE);
		}
	}

	private void setUpBackBtn() {
		Button backBtn = findViewById(R.id.backBtn_tasklist);
		backBtn.setOnClickListener(view -> finish());
	}

	private void setUpAddTaskFAB() {
		FloatingActionButton addTaskFAB = findViewById(R.id.addTaskFAB);
		addTaskFAB.setOnClickListener(v -> {
			AddTaskDialog taskDialog = new AddTaskDialog();
			taskDialog.showDialog(TaskActivity.this);
		});
	}

	/**
	 * Custom dialog to add tasks
	 * */
	public class AddTaskDialog {

		public void showDialog(Activity activity) {
			final Dialog dialog = new Dialog(activity);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setCancelable(false);
			dialog.setContentView(R.layout.add_task_dialog);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

			EditText nameInput = dialog.findViewById(R.id.add_task_edit_text);

			FloatingActionButton cancelFab = dialog.findViewById(R.id.cancel_add_task_fab);
			cancelFab.setOnClickListener(getCancelFabListener(dialog));

			FloatingActionButton addFab = dialog.findViewById(R.id.save_add_task_fab);
			addFab.setOnClickListener(getAddFabListener(dialog, nameInput));

			dialog.show();
		}

		private View.OnClickListener getAddFabListener(Dialog dialog, EditText nameInput) {
			return (view) -> {
				if (nameInput.getText().toString().isEmpty()) {
					Toast.makeText(TaskActivity.this, R.string.task_name_error_toast_message, Toast.LENGTH_SHORT).show();
				}
				else {
					options.addTask(TaskActivity.this, new Task(nameInput.getText().toString(), options.getChildList(TaskActivity.this).size()));

					populateTaskList();
					setUpListItemClickListener();

					dialog.cancel();
				}
			};
		}

		private View.OnClickListener getCancelFabListener(Dialog dialog) {
			return (view) -> dialog.dismiss();
		}
	}

	/**
	 * Custom dialog to edit task
	 * */
	public class EditTaskDialog {

		public void showDialog(Activity activity, int index) {
			final Dialog dialog = new Dialog(activity);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setCancelable(false);
			dialog.setContentView(R.layout.edit_task_dialog);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

			EditText nameInput = dialog.findViewById(R.id.edit_task_edit_text);
			nameInput.setText(options.getTaskList(TaskActivity.this).get(index).getTaskName());

			FloatingActionButton cancelFab = dialog.findViewById(R.id.cancel_edit_task_fab);
			cancelFab.setOnClickListener(getCancelFabListener(dialog));

			FloatingActionButton addFab = dialog.findViewById(R.id.edit_task_save_fab);
			addFab.setOnClickListener(getAddFabListener(dialog, nameInput, index));

			FloatingActionButton deleteFab = dialog.findViewById(R.id.delete_task_fab);
			deleteFab.setOnClickListener(getDeleteFabListener(dialog, index));

			dialog.show();
		}

		private View.OnClickListener getCancelFabListener(Dialog dialog) {
			return (view) -> dialog.dismiss();
		}

		private View.OnClickListener getAddFabListener(Dialog dialog, EditText nameInput, int index) {
			return (view) -> {
				if (nameInput.getText().toString().isEmpty()) {
					Toast.makeText(TaskActivity.this, R.string.task_name_error_toast_message, Toast.LENGTH_SHORT).show();
				}
				else {
					options.editTaskName(TaskActivity.this, nameInput.getText().toString(), index);
					populateTaskList();
					setUpListItemClickListener();
					dialog.cancel();
				}
			};
		}

		private View.OnClickListener getDeleteFabListener(Dialog dialog, int index) {
			return (view) -> {
				options.removeTask(TaskActivity.this, index);
				populateTaskList();
				setUpListItemClickListener();
				dialog.cancel();
			};
		}
	}


	private void populateTaskList() {
		TaskListAdapter adapter = new TaskListAdapter();
		ListView taskList = findViewById(R.id.taskListView);
		taskList.setAdapter(adapter);
		taskList.setDivider(null);
		taskList.setDividerHeight(20);
		setUpListItemClickListener();
		setUpEmptyMessage();
	}

	/**
	 * Custom adapter for tasks
	 * */
	private class TaskListAdapter extends ArrayAdapter<Task> {

		public TaskListAdapter() {
			super(TaskActivity.this, R.layout.task_view, options.getTaskList(TaskActivity.this));
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View taskView = convertView;
			if (taskView == null) {
				taskView = getLayoutInflater().inflate(R.layout.task_view, parent, false);
			}

			Task currentTask = options.getTaskList(TaskActivity.this).get(position);

			// set up game ListView item
			TextView taskName = taskView.findViewById(R.id.taskName);
			taskName.setText(currentTask.getTaskName());

			Button editButton = taskView.findViewById(R.id.edit_task_button);
			editButton.setOnClickListener(view -> {
				EditTaskDialog editTaskDialog = new EditTaskDialog();
				editTaskDialog.showDialog(TaskActivity.this, position);
			});

			ImageView childImage = taskView.findViewById(R.id.child_image_task_list);
			if (currentTask.getCurrentChildIndex() >= 0) {
				Child currentChild = options.getChildList(TaskActivity.this).get(currentTask.getCurrentChildIndex());
				if (currentChild.getEncodedImage() != null) {
					childImage.setImageBitmap(currentChild.getImageBitmap());
				}
			}

			TextView childName = taskView.findViewById(R.id.childNameInTaskList);
			childName.setText(options.getChildName(TaskActivity.this, currentTask.getCurrentChildIndex()));
			return taskView;
		}

	}

	private void setUpListItemClickListener() {
		if (options.getTaskList(this).size() == 0) {
			return;
		}
		ListView taskListView = findViewById(R.id.taskListView);
		taskListView.setOnItemClickListener((adapterView, taskClicked, position, id) -> {
			if (options.getTaskList(TaskActivity.this).get(position).getCurrentChildIndex() != -1) {
				ConfirmTaskDoneDialog dialog = new ConfirmTaskDoneDialog();
				dialog.showDialog(TaskActivity.this, position);
			}
		});
	}

	/**
	 * Custom dialog to confirm task
	 * */
	public class ConfirmTaskDoneDialog {

		public void showDialog(Activity activity, int index) {
			final Dialog dialog = new Dialog(activity);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setCancelable(false);
			dialog.setContentView(R.layout.confirm_task_done_dialog);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

			FloatingActionButton cancelFab = dialog.findViewById(R.id.cancelfab6);
			cancelFab.setOnClickListener(getCancelFabListener(dialog));

			FloatingActionButton confirmFab = dialog.findViewById(R.id.confirmfab);
			confirmFab.setOnClickListener(getAddFabListener(dialog, index));

			Task task = options.getTaskList(TaskActivity.this).get(index);

			ImageView childImage = dialog.findViewById(R.id.child_image_confirm_dialog);
			if (task.getCurrentChildIndex() >= 0) {
				Child currentChild = options.getChildList(TaskActivity.this).get(task.getCurrentChildIndex());
				if (currentChild.getEncodedImage() != null) {
					childImage.setImageBitmap(currentChild.getImageBitmap());
				}
			}

			TextView confirmText = dialog.findViewById(R.id.confirm_message);
			confirmText.setText(getString(R.string.confirm_message, options.getChildName(TaskActivity.this, task.getCurrentChildIndex()), task.getTaskName()));
			dialog.show();

			FloatingActionButton showHistoryButton = dialog.findViewById(R.id.showHistoryButton);
			showHistoryButton.setOnClickListener(getShowHistoryListener(index));
		}

		private View.OnClickListener getCancelFabListener(Dialog dialog) {
			return (view) -> dialog.dismiss();
		}

		private View.OnClickListener getAddFabListener(Dialog dialog, int index) {
			return (view) -> {
				options.assignTaskToNextChild(TaskActivity.this, index);
				populateTaskList();
				dialog.dismiss();
			};
		}

		private View.OnClickListener getShowHistoryListener(int index){
			return (view) -> {
				Intent intent = TaskHistoryActivity.makeIntent(TaskActivity.this, index);
				startActivity(intent);
			};
		}

	}

}