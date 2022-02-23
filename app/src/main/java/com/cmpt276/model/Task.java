package com.cmpt276.model;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Object for storing individual tasks. Does not actually store a list of children,
 * but is meant to store the indices of who has to do what.
 * */
public class Task {
	private static final int NO_CHILD = -1;
	private static final int DELETED_CHILD = -2;

	private String taskName;
	private int currentChildIndex;
	private ArrayList<TaskHistory> taskHistoryList;

	public Task(String taskName, int numberOfChildren) {
		this.taskName = taskName;
		setChildIndex(numberOfChildren);
		taskHistoryList =  new ArrayList<>();
	}

	private void setChildIndex(int numberOfChildren) {
		if (numberOfChildren == 0) {
			currentChildIndex = NO_CHILD;
		}
		else {
			currentChildIndex = ThreadLocalRandom.current().nextInt(0, numberOfChildren);
		}

	}

	public void editTaskName(String newTaskName) {
		taskName = newTaskName;
	}

	public String getTaskName() {
		return taskName;
	}

	public int getCurrentChildIndex() {
		return currentChildIndex;
	}

	public void incrementNextChildIndex(int childListSize) {
		currentChildIndex = (currentChildIndex + 1) % childListSize;
	}

	public void updateIndexOnChildDelete(int deletedChildIndex, int childListSize) {
		if (childListSize == 0) {
			currentChildIndex = NO_CHILD;
		}
		else {
			if (deletedChildIndex < currentChildIndex) {
				currentChildIndex--;
			}
			else if (deletedChildIndex == childListSize) {
				currentChildIndex = 0;
			}
		}
	}

	public void updateHistoryIndexOnChildDelete(int deletedChildIndex) {
		for (TaskHistory taskHistory : taskHistoryList) {
			if (taskHistory.getChildIndex() == deletedChildIndex) {
				taskHistory.setChildIndex(DELETED_CHILD);
			}
			else if (deletedChildIndex < taskHistory.getChildIndex()){
				taskHistory.decrementChildIndex();
			}
		}

	}

	public void addTaskHistory(int childIndex){
		TaskHistory taskHistory = new TaskHistory(childIndex);
		taskHistoryList.add(taskHistory);
	}

	public ArrayList<TaskHistory> getTaskHistory(){
		return taskHistoryList;
	}

	public void clearHistory(){
		taskHistoryList.clear();
	}
}
