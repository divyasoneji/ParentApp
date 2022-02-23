package com.cmpt276.parentapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.cmpt276.model.Child;
import com.cmpt276.model.Coin;
import com.cmpt276.model.Task;
import com.cmpt276.model.TaskHistory;
import com.cmpt276.parentapp.serializer.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;

/**
 * Options class implement Shared Preferences to save data between app runs
 */
public class Options {

	private static Options instance;

	private static Random idGenerator = new Random();

	private static final String PREFS_TAG = "SharedPrefs";
	private static final String CHILD_TAG = "Child";
	private static final String TASK_TAG = "Task";
	private static final int NO_CHILD = -1;
	private static final int DELETED_CHILD = -2;


	private static final String FLIP_LIST_TAG = "FlipList";
	private static final String FLIP_QUEUE_TAG = "FlipQueue";
	private static final String NO_CHILD_FLIPPING = "NoChildFlipping";

	private static final String NUMBER_OF_BREATHS_TAG = "NumberOfBreaths";

	private static final Type TYPE_CHILD_LIST = new TypeToken<ArrayList<Child>>() {
	}.getType();
	private static final Type TYPE_TASK_LIST = new TypeToken<ArrayList<Task>>() {
	}.getType();
	private static final Type TYPE_INT_LIST = new TypeToken<ArrayList<Integer>>() {
	}.getType();
	private static final Type TYPE_COIN_LIST = new TypeToken<ArrayList<Coin>>() {
	}.getType();

	private Options() {
	}


	public static Options getInstance() {
		if (instance == null) {
			instance = new Options();
		}
		return instance;
	}

	public void addChild(Context context, String newChildName) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		String jsonString = pref.getString(CHILD_TAG, null);

		Gson gson = new Gson();

		ArrayList<Child> children;
		if (jsonString == null) {
			children = new ArrayList<>();
		}
		else {
			children = gson.fromJson(jsonString, TYPE_CHILD_LIST);
		}

		Child child = new Child(newChildName, generateID(children));
		children.add(child);

		String newJsonString = gson.toJson(children);
		editor.putString(CHILD_TAG, newJsonString);
		editor.apply();

		checkNoChildrenInTaskList(context);
	}

	public void addChild(Context context, String newChildName, String encodedImage) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		String jsonString = pref.getString(CHILD_TAG, null);

		Gson gson = new Gson();

		ArrayList<Child> children;
		if (jsonString == null) {
			children = new ArrayList<>();
		}
		else {
			children = gson.fromJson(jsonString, TYPE_CHILD_LIST);
		}

		Child child = new Child(newChildName, encodedImage, generateID(children));
		children.add(child);

		String newJsonString = gson.toJson(children);
		editor.putString(CHILD_TAG, newJsonString);
		editor.apply();

		checkNoChildrenInTaskList(context);
	}

	public void removeChild(Context context, int index) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		String jsonString = pref.getString(CHILD_TAG, null);

		Gson gson = new Gson();

		ArrayList<Child> list;
		if (jsonString == null) {
			list = new ArrayList<>();
		}
		else {
			list = gson.fromJson(jsonString, TYPE_CHILD_LIST);
		}

		if (index < 0 || index >= list.size()) {
			throw new IllegalArgumentException("Cannot remove child that is out of bounds.");
		}

		list.remove(index);

		String newJsonString = gson.toJson(list);
		editor.putString(CHILD_TAG, newJsonString);
		editor.apply();

		updateTasksOnChildDelete(context, list, index);
	}

	private void updateTasksOnChildDelete(Context context, ArrayList<Child> children, int deletedChildIndex) {

		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		String jsonString = pref.getString(TASK_TAG, null);
		Gson gson = new Gson();

		ArrayList<Task> tasks;
		if (jsonString == null) {
			tasks = new ArrayList<>();
		}
		else {
			tasks = gson.fromJson(jsonString, TYPE_TASK_LIST);
		}

		for (Task task : tasks) {
			task.updateIndexOnChildDelete(deletedChildIndex, children.size());
			task.updateHistoryIndexOnChildDelete(deletedChildIndex);
		}

		String newJsonString = gson.toJson(tasks);
		editor.putString(TASK_TAG, newJsonString);
		editor.apply();
	}

	public void editChildName(Context context, int index, String name) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		String jsonString = pref.getString(CHILD_TAG, null);

		Gson gson = new Gson();

		ArrayList<Child> list;
		if (jsonString == null) {
			list = new ArrayList<>();
		}
		else {
			list = gson.fromJson(jsonString, TYPE_CHILD_LIST);
		}

		if (index < 0 || index >= list.size()) {
			throw new IllegalArgumentException("Cannot edit child that is out of bounds.");
		}

		Child child = list.get(index);
		child.setName(name);

		String newJsonString = gson.toJson(list);
		editor.putString(CHILD_TAG, newJsonString);
		editor.apply();
	}

	public void editChildImage(Context context, int index, String encodedBitmap) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		String jsonString = pref.getString(CHILD_TAG, null);

		Gson gson = new Gson();

		ArrayList<Child> list;
		if (jsonString == null) {
			list = new ArrayList<>();
		}
		else {
			list = gson.fromJson(jsonString, TYPE_CHILD_LIST);
		}

		if (index < 0 || index >= list.size()) {
			throw new IllegalArgumentException("Cannot edit child that is out of bounds.");
		}
		list.get(index).setEncodedImage(encodedBitmap);
		String newJsonString = gson.toJson(list);
		editor.putString(CHILD_TAG, newJsonString);
		editor.apply();
	}

	public ArrayList<Child> getChildList(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		String jsonString = pref.getString(CHILD_TAG, null);

		Gson gson = new Gson();

		ArrayList<Child> list;
		if (jsonString == null) {
			list = new ArrayList<>();
		}
		else {
			list = gson.fromJson(jsonString, TYPE_CHILD_LIST);
		}
		return list;
	}

	/*
	 * the queue of children is an array of indices which correspond to the children stored
	 *
	 * CHILDREN: [Bob(0), Joe(1), Jefferey(2), Jacob(3), Jimothy(4)]
	 * QUEUE: [1, 3, 4, 2, 0]
	 * indicates that the order should be Joe, Jacob, Jimothy, Jefferey, Bob
	 *
	 * the operations we can do to the queue is to
	 * - get the queue
	 * - move an element to the front
	 * - move the front of the queue to the back
	 *
	 * Terminology:
	 * queue index - index of queue array (e.g. queue index of 2 in the above example has value 4)
	 * */
	public ArrayList<Integer> getQueueOrder(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		String jsonString = pref.getString(FLIP_QUEUE_TAG, null);

		Gson gson = new Gson();

		ArrayList<Integer> queue = gson.fromJson(jsonString, TYPE_INT_LIST);
		ArrayList<Child> children = getChildList(context);

		if (queue == null) {
			queue = new ArrayList<>();
			for (int i = 0; i < children.size(); i++) {
				queue.add(i);
			}
		}
		else if (queue.size() > children.size()) {
			//remove all indices that are greater than the current child list size to prevent out of bounds errors when removing children
			for (int i = 0; i < queue.size(); i++) {
				if (queue.get(i) >= children.size()) {
					queue.remove(i--);
				}
			}
		}
		else if (queue.size() < children.size()) {
			//append the missing indices in order up to the number of children there are
			for (int i = queue.size(); i < children.size(); i++) {
				queue.add(i);
			}
		}

		String newQueueString = gson.toJson(queue, TYPE_INT_LIST);
		editor.putString(FLIP_QUEUE_TAG, newQueueString);
		editor.apply();

		return queue;
	}


	//moves the given queue index element to the front
	public void moveToFrontOfQueue(Context context, int index) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		ArrayList<Integer> queue = getQueueOrder(context);

		int element = queue.remove(index);
		queue.add(0, element);

		Gson gson = new Gson();

		String newQueueString = gson.toJson(queue, TYPE_INT_LIST);
		editor.putString(FLIP_QUEUE_TAG, newQueueString);
		editor.apply();
	}

	//moves the element at the front of the queue to the back, thus advancing the queue
	public void advanceQueue(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		ArrayList<Integer> queue = getQueueOrder(context);

		int element = queue.remove(0);
		queue.add(element);

		Gson gson = new Gson();

		String newQueueString = gson.toJson(queue, TYPE_INT_LIST);
		editor.putString(FLIP_QUEUE_TAG, newQueueString);
		editor.apply();
	}

	//Returns true if the current setting is that no child is selected for flipping
	//Does not interfere with queue order in any way.
	public boolean isNoChildFlipping(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		boolean isFlipping = pref.getBoolean(NO_CHILD_FLIPPING, false);
		return isFlipping;
	}

	public void setNoChildFlipping(Context context, boolean isFlipping) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		editor.putBoolean(NO_CHILD_FLIPPING, isFlipping);
		editor.apply();
	}

	public void addCoinFlip(Context context, Coin coin) {
		//pulls json encoded array from shared preferences, adds the coin to the list, then
		//re-encodes it to json to be sent back to preferences
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		String jsonString = pref.getString(FLIP_LIST_TAG, null);

		Gson gson = new GsonBuilder()
				.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
				.create();

		ArrayList<Coin> flipHistory;

		if (jsonString == null) {
			flipHistory = new ArrayList<>();
		}
		else {
			flipHistory = gson.fromJson(jsonString, TYPE_COIN_LIST);
		}
		flipHistory.add(coin);

		jsonString = gson.toJson(flipHistory);
		editor.putString(FLIP_LIST_TAG, jsonString);

		editor.apply();
	}

	public ArrayList<Coin> getFlipHistory(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		String jsonString = pref.getString(FLIP_LIST_TAG, null);
		if (jsonString == null) {
			return new ArrayList<>();
		}

		Gson gson = new GsonBuilder()
				.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
				.create();

		ArrayList<Coin> history = gson.fromJson(jsonString, TYPE_COIN_LIST);
		ArrayList<Child> children = getChildList(context);

		//if a child's name is changed in the settings, the corresponding entry must be changed in the history
		//There is a chance that a child can be deleted, and another child can be made with
		//the same ID before checking the history, thereby rewriting the history with the new child,
		//but the odds are very, very low.
		for (Coin coin : history) {
			Child coinChild = coin.getChild();
			for (Child child : children) {
				if (coinChild.getId() == child.getId()) {
					coinChild.setName(child.getName());
				}
			}
		}

		String newJsonString = gson.toJson(history, TYPE_COIN_LIST);
		editor.putString(FLIP_LIST_TAG, newJsonString);
		editor.apply();

		return history;
	}

	public void clearCoinFlips(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.remove(FLIP_LIST_TAG);

		editor.apply();
	}


	private static long generateID(ArrayList<Child> children) {
		boolean isValidId = false;
		long newId = 0;
		//generate new IDs that are not the same as an existing ID
		while (!isValidId) {
			isValidId = true;
			newId = idGenerator.nextInt();
			//prevent the reserved empty child from being overwritten
			//TODO: make this a constant that makes sense in the grand context
			if (newId == 0) {
				continue;
			}
			for (Child child : children) {
				if (newId == child.getId()) {
					isValidId = false;
					break;
				}
			}
		}
		return newId;
	}

	public void addTask(Context context, Task task) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		String jsonString = pref.getString(TASK_TAG, null);
		Gson gson = new Gson();

		ArrayList<Task> tasks;
		if (jsonString == null) {
			tasks = new ArrayList<>();
		}
		else {
			tasks = gson.fromJson(jsonString, TYPE_TASK_LIST);
		}

		tasks.add(task);

		String newJsonString = gson.toJson(tasks);
		editor.putString(TASK_TAG, newJsonString);
		editor.apply();
	}

	public void removeTask(Context context, int index) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		String jsonString = pref.getString(TASK_TAG, null);

		Gson gson = new Gson();

		ArrayList<Task> tasks;
		if (jsonString == null) {
			tasks = new ArrayList<>();
		}
		else {
			tasks = gson.fromJson(jsonString, TYPE_TASK_LIST);
		}

		if (index < 0 || index >= tasks.size()) {
			throw new IllegalArgumentException("Cannot remove task that is out of bounds.");
		}

		tasks.remove(index);

		String newJsonString = gson.toJson(tasks);
		editor.putString(TASK_TAG, newJsonString);
		editor.apply();
	}

	public void editTaskName(Context context, String newTaskName, int taskIndex) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		String jsonString = pref.getString(TASK_TAG, null);
		Gson gson = new Gson();

		ArrayList<Task> tasks;
		if (jsonString == null) {
			tasks = new ArrayList<>();
		}
		else {
			tasks = gson.fromJson(jsonString, TYPE_TASK_LIST);
		}


		tasks.get(taskIndex).editTaskName(newTaskName);

		String newJsonString = gson.toJson(tasks);
		editor.putString(TASK_TAG, newJsonString);
		editor.apply();

	}


	private void checkNoChildrenInTaskList(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		String jsonString = pref.getString(TASK_TAG, null);
		Gson gson = new Gson();

		ArrayList<Task> tasks;
		if (jsonString == null) {
			tasks = new ArrayList<>();
		}
		else {
			tasks = gson.fromJson(jsonString, TYPE_TASK_LIST);
		}

		if ((getChildList(context).size() - 1) == 0) {
			for (Task task : tasks) {
				task.incrementNextChildIndex(getChildList(context).size());
			}
		}

		String newJsonString = gson.toJson(tasks);
		editor.putString(TASK_TAG, newJsonString);
		editor.apply();
	}

	public String getChildName(Context context, int childIndex) {
		if (childIndex == NO_CHILD) {
			return context.getString(R.string.no_child_added_yet);
		}
		else if (childIndex == DELETED_CHILD){
			return "Deleted Child";
		}
		else {
			return getChildList(context).get(childIndex).getName();
		}
	}

	public void assignTaskToNextChild(Context context, int taskIndex) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		String jsonString = pref.getString(TASK_TAG, null);
		Gson gson = new Gson();

		ArrayList<Task> tasks;
		if (jsonString == null) {
			tasks = new ArrayList<>();
		}
		else {
			tasks = gson.fromJson(jsonString, TYPE_TASK_LIST);
		}

		Task task = tasks.get(taskIndex);
		task.addTaskHistory(task.getCurrentChildIndex());
		task.incrementNextChildIndex(getChildList(context).size());

		String newJsonString = gson.toJson(tasks);
		editor.putString(TASK_TAG, newJsonString);
		editor.apply();

	}

	//Get Task List to Shared Prefs
	public ArrayList<Task> getTaskList(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		String jsonString = pref.getString(TASK_TAG, null);

		Gson gson = new Gson();
		ArrayList<Task> list;

		if (jsonString == null) {
			list = new ArrayList<>();
		}
		else {
			list = gson.fromJson(jsonString, TYPE_TASK_LIST);
		}

		return list;
	}

	public ArrayList<TaskHistory> getTaskHistoryList(Context context, int taskIndex){
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		String jsonString = pref.getString(TASK_TAG, null);

		Gson gson = new Gson();
		ArrayList<Task> list;
		ArrayList<TaskHistory> historyList;

		if (jsonString == null) {
			historyList = new ArrayList<>();
		}
		else {
			list = gson.fromJson(jsonString, TYPE_TASK_LIST);
			historyList = list.get(taskIndex).getTaskHistory();
		}

		return historyList;
	}

	public void clearTaskHistory(Context context, int taskIndex) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		String jsonString = pref.getString(TASK_TAG, null);

		Gson gson = new Gson();

		ArrayList<Task> tasks;
		if (jsonString == null) {
			tasks = new ArrayList<>();
		} else {
			tasks = gson.fromJson(jsonString, TYPE_TASK_LIST);
			tasks.get(taskIndex).clearHistory();
		}

		String newJsonString = gson.toJson(tasks);
		editor.putString(TASK_TAG, newJsonString);
		editor.apply();
	}
	public int getNumberOfBreaths(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		int numberOfBreaths = pref.getInt(NUMBER_OF_BREATHS_TAG, 0);
		return numberOfBreaths;
	}

	public void setNumberOfBreaths(Context context, int numberOfBreaths) {
		SharedPreferences pref = context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		editor.putInt(NUMBER_OF_BREATHS_TAG, numberOfBreaths);

		editor.apply();
	}
}
