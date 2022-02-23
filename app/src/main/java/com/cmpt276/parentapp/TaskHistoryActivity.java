package com.cmpt276.parentapp;

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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cmpt276.model.Child;
import com.cmpt276.model.Task;
import com.cmpt276.model.TaskHistory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class TaskHistoryActivity extends AppCompatActivity {

    public static final String TASK_INDEX = "com.cmpt276.parentapp - taskIndex";
    Options options;
    int taskIndex;
    Task currentTask;
    ArrayList<TaskHistory> taskHistoryArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_history);
        options = Options.getInstance();
        extractDataFromIntent();
        currentTask = options.getTaskList(TaskHistoryActivity.this).get(taskIndex);
        taskHistoryArrayList = currentTask.getTaskHistory();
        setUpTaskTitle();
        setUpEmptyMessage();
        populateTaskHistoryList();

        setUpBackBtn();
        setUpClearBtn();

    }

    private void setUpTaskTitle(){
        TextView title = findViewById(R.id.taskHistoryTitle);
        title.setText(currentTask.getTaskName());
    }

    private void populateTaskHistoryList() {
        TaskHistoryListViewAdapter adapter = new TaskHistoryListViewAdapter();
        ListView historyList = findViewById(R.id.listViewTaskHistory);
        historyList.setAdapter(adapter);
        historyList.setDivider(null);
        historyList.setDividerHeight(20);
    }

    public void setUpEmptyMessage(){
        TextView emptyMessage = findViewById(R.id.emptyHistoryMessage);
        if (taskHistoryArrayList.isEmpty()){
            emptyMessage.setVisibility(View.VISIBLE);
        }
        else {
            emptyMessage.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Custom adapter for task history in listview
     * */
    private class TaskHistoryListViewAdapter extends ArrayAdapter<TaskHistory> {

        public TaskHistoryListViewAdapter() {
            super(TaskHistoryActivity.this, R.layout.task_history_view, taskHistoryArrayList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View taskHistoryView = convertView;
            if (taskHistoryView == null) {
                taskHistoryView = getLayoutInflater().inflate(R.layout.task_history_view, parent, false);
            }

            TaskHistory currentTaskHistory = taskHistoryArrayList.get(position);

            // set up game ListView item
            TextView childName = taskHistoryView.findViewById(R.id.childNameTextView);
            childName.setText(options.getChildName(TaskHistoryActivity.this, currentTaskHistory.getChildIndex()));

            ImageView childImage = taskHistoryView.findViewById(R.id.child_image_task_history_list);
            if (currentTaskHistory.getChildIndex() >= 0) {
                Child currentChild = options.getChildList(TaskHistoryActivity.this).get(currentTaskHistory.getChildIndex());
                if (currentChild.getEncodedImage() != null) {
                    childImage.setImageBitmap(currentChild.getImageBitmap());
                }
            }

            TextView date = taskHistoryView.findViewById(R.id.dateTextView);
            date.setText(currentTaskHistory.getDateTaskDone());
            return taskHistoryView;
        }
    }

    private void setUpBackBtn() {
        Button backBtn = findViewById(R.id.backBtn_TaskHistory);
        backBtn.setText(R.string.back);
        backBtn.setOnClickListener((view) -> finish());
    }

    private void setUpClearBtn() {
        Button buttonClear = findViewById(R.id.buttonClearTaskHistory);
        buttonClear.setOnClickListener((view) -> {
            if (taskHistoryArrayList.size() == 0) {
                Toast.makeText(this, getString(R.string.no_history_to_delete), Toast.LENGTH_SHORT).show();
            }
            else {
                CLearTaskHistoryDialog alert = new CLearTaskHistoryDialog();
                alert.showDialog(TaskHistoryActivity.this);
            }
        });
    }

    /**
     * Custom dialog to clear history
     * */
    public class CLearTaskHistoryDialog {

        public void showDialog(Activity activity) {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.clear_dialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            TextView title = dialog.findViewById(R.id.clearDialogTitle);
            title.setText(R.string.clear_task_history_message);

            FloatingActionButton cancelFab = dialog.findViewById(R.id.cancelFab);
            cancelFab.setOnClickListener(getCancelFabListener(dialog));

            FloatingActionButton addFab = dialog.findViewById(R.id.okFab);
            addFab.setOnClickListener(getAddFabListener(dialog));

            dialog.show();
        }

        private View.OnClickListener getCancelFabListener(Dialog dialog) {
            return (view) -> dialog.dismiss();
        }

        private View.OnClickListener getAddFabListener(Dialog dialog) {
            return (view) -> {
                options.clearTaskHistory(TaskHistoryActivity.this, taskIndex);
                dialog.dismiss();
                TaskHistoryActivity.this.finish();
            };
        }
    }

    public static Intent makeIntent(Context context, int taskIndex) {
        Intent intent = new Intent(context, TaskHistoryActivity.class);
        intent.putExtra(TASK_INDEX, taskIndex);
        return intent;
    }

    public void extractDataFromIntent(){
        Intent intent = getIntent();
        taskIndex = intent.getIntExtra(TASK_INDEX, -1);
    }

}