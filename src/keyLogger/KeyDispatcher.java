/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keyLogger;

import java.util.ArrayList;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseInputListener;
import org.jnativehook.mouse.NativeMouseWheelEvent;
import org.jnativehook.mouse.NativeMouseWheelListener;

/**
 *
 * @author Mukesh
 */
public class KeyDispatcher implements NativeKeyListener, NativeMouseListener, NativeMouseWheelListener, NativeMouseInputListener  {

    private String act = "n";
    private ArrayList<String> al = new ArrayList<String>();

    public KeyDispatcher() {
        try {
            GlobalScreen.registerNativeHook();
            //System.out.println("Key Logger Success.");
        } catch (NativeHookException ex) {
            System.out.println("There was a problem registering the native hook.");
            // System.err.println(ex.getMessage());
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {

        act = "k";
      // System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));

        /*if (e.getKeyCode() == NativeKeyEvent.VK_ESCAPE) {
         GlobalScreen.unregisterNativeHook();
         }*/
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {

        // System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
         //keyPressedCtr++;
        // System.out.println("Key Typed: " + e.getKeyText(e.getKeyCode()));
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent nme) {

        act = "m";
        
       // System.out.println("Mosue Clicked: " + nme.getClickCount());
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent nme) {

        act = "m";
      //  System.out.println("Mosue Pressed: " + nme.getButton());
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent nme) {
        // System.out.println("Mosue Released: " + nme.getButton());
    }

    @Override
    public void nativeMouseWheelMoved(NativeMouseWheelEvent e) {
        act = "m";
        // System.out.println("Mosue Wheel Moved: " + e.getWheelRotation());
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent e) {
        act = "m";
        //System.out.println("Mosue Moved: " + e.getX() + ", " + e.getY());
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent e) {
        act = "m";
       // System.out.println("Mosue Dragged: " + e.getX() + ", " + e.getY());
    }

    public void reset() {
        act = "n";
        al.clear();
    }

    public int[] getUserAct() {
        int pct = 0;
        double mouse = 0;
        double keys = 0;
        int[] objArray = new int[3];
        for (String temp : al) {
            if (temp == "m") {
                mouse++;
            }
            if (temp == "k") {
                keys++;
            }
        }
        objArray[0] = (int) (Math.round(mouse / 60.0 * 100));
        objArray[1] = (int) (Math.round(keys / 60.0 * 100));
        pct = objArray[0]+objArray[1];
        objArray[2] = (int) (pct);
        return objArray;
    }

    public void setKey() {
        al.add(act);
        act = "n";
    }

    public String returnKey() {
        return act;
    }

}
