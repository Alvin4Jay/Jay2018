package JavaMultiThread.Chapter4;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 将线程安全性委托给多个状态变量
 */
public class VisualComponent {
    private final List<KeyListener> keyListeners = new CopyOnWriteArrayList<>(); //同步
    private final List<MouseListener> mouseListener = new CopyOnWriteArrayList<>(); //同步

    public void addKeyListener(KeyListener listener){
        keyListeners.add(listener);
    }

    public void addMoueListener(MouseListener listener){
        mouseListener.add(listener);
    }

    public void removeKeyListener(KeyListener listener){
        keyListeners.remove(listener);
    }

    public void removeMOuseListener(MouseListener listener){
        mouseListener.remove(listener);
    }

}
