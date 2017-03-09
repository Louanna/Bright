package com.movit.platform.framework.widget.swipeLayout.interfaces;

import java.util.List;

import com.movit.platform.framework.widget.swipeLayout.SwipeLayout;
import com.movit.platform.framework.widget.swipeLayout.implments.SwipeItemMangerImpl;

public interface SwipeItemMangerInterface {

    public void openItem(int position);

    public void closeItem(int position);

    public void closeAllExcept(SwipeLayout layout);

    public List<Integer> getOpenItems();

    public List<SwipeLayout> getOpenLayouts();

    public void removeShownLayouts(SwipeLayout layout);

    public boolean isOpen(int position);

    public SwipeItemMangerImpl.Mode getMode();

    public void setMode(SwipeItemMangerImpl.Mode mode);
}
