package com.zenith.feature.gui.elements;

import com.zenith.feature.gui.Gui;
import com.zenith.feature.gui.Page;

@FunctionalInterface
public interface ButtonClickHandler {
    void accept(Button button, Gui gui, final Page page, int index, boolean leftClick);
}
