package com.apw.drivedemo;

import java.util.TimerTask;

public class TimerRepaint extends TimerTask {
    protected DriveTest window;
    public TimerRepaint(DriveTest window){
        super();
        this.window = window;
    }

    @Override
    public void run() {
        window.updateWindow();
        window.repaint();
    }
}
