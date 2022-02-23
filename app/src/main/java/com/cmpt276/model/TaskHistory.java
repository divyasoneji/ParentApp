package com.cmpt276.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TaskHistory {
    String dateTaskDone;
    int childIndex;

    public TaskHistory(int childIndex){
        this.childIndex = childIndex;
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a");
        dateTaskDone = localDateTime.format(formatter);

    }
     public void setChildIndex(int newIndex){
        childIndex = newIndex;
     }

    public void decrementChildIndex(){
        childIndex = childIndex - 1;
    }

    public String getDateTaskDone() {
        return dateTaskDone;
    }

    public int getChildIndex() {
        return childIndex;
    }

}
