package com.fisincorporated.exercisetracker.ui.master;

import android.support.annotation.ColorInt;

public interface IChangeToolbar {

    public void setToolbarColor(@ColorInt int color);
    public void resetToolbarColorToDefault();

    public void setToolbarTitle(String title);
    public void resetToolbarTitleToDefault();
}
