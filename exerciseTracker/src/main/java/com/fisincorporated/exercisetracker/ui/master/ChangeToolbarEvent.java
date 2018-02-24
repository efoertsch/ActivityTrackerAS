package com.fisincorporated.exercisetracker.ui.master;



import android.support.annotation.ColorInt;

public class ChangeToolbarEvent {
    public enum EVENT {
        SET_TOOLBAR_COLOR,
        RESET_TOOLBAR_COLOR_TO_DEFAULT,
        SET_TOOLBAR_TITLE,
        RESET_TOOLBAR_TITLE_TO_DEFAULT;
    }

    private EVENT event;
    private  @ColorInt
    int color;
    private String title;

    public ChangeToolbarEvent(EVENT event) {
        this.event = event;
    }

    public EVENT getEvent() {
        return event;
    }

    public ChangeToolbarEvent setToolbarColor(@ColorInt int color) {
        this.color = color;
        return this;
    }

    public int getColor() {
        return color;
    }

    public ChangeToolbarEvent setToolbarTitle(String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }

}
