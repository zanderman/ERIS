package com.eris.classes;

/**
 * Created by David on 10/29/16.
 */

public class NavigationDrawerMenuItem {
    public String name;
    public int iconResourceId;
    public String associatedFragmentClassName;
    public String associatedFragmentClassSimpleName;

    public NavigationDrawerMenuItem(String name, int iconResourceId,
                                    String associatedFragmentClassName,
                                    String associatedFragmentClassSimpleName) {
        this.name = name;
        this.iconResourceId = iconResourceId;
        this.associatedFragmentClassName = associatedFragmentClassName;
        this.associatedFragmentClassSimpleName = associatedFragmentClassSimpleName;
    }
}
