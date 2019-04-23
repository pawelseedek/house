package com.packt;

import com.pinout.InOut;

public class App 
{
    public static void main( String[] args )
    {
    	SynchronizedLinkedList kolejka = new SynchronizedLinkedList<>();
		InOut gpio = new InOut(kolejka);
        Thread PinThread = new Thread(gpio);
        PinThread.start();
    	Tcp tcp = new Tcp("secretid","secretpass", gpio, kolejka );
    	gpio.setTcpListeners(tcp);
    }
}
