package com.pinout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.packt.Gpio;
import com.packt.Immutable;
import com.packt.SynchronizedLinkedList;
import com.packt.Tcp;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;


public class InOut implements Runnable, Gpio{

   	private final GpioController gpio = GpioFactory.getInstance();	
    private static Map<String, GpioPinDigitalOutput> outputs = new HashMap();
	private static Map<String, GpioPinDigitalInput> inputs = new HashMap();
	private int numberOfOutputs = 20;
	private int numberOfInputs = 8;
	private List<Listener> listeners = new ArrayList<>();
	private static List<GpioPinDigitalInput> inputListeners;
	private Immutable immutable = new Immutable();
	
	private SynchronizedLinkedList kolejka;
	Tcp tcp;
	
	public InOut(SynchronizedLinkedList kolejka){
		this.kolejka = kolejka;
	}
	
	
	@Override
    public void run() {
		while(true){
            if(kolejka.isEmpty() == false){
				String msg = kolejka.getFirst().toString();
				if(msg.contains(immutable.PINCONFIG)){
					msg = msg.substring(28);
					this.configPins(msg);
					kolejka.removeFirst();
				}else {
					String pinNumber = kolejka.getFirst().toString();
					changePinState(pinNumber);
					kolejka.removeFirst();
					for(Listener listener : listeners){
						listener.pinChanged(pinNumber);
					}
				}
			}
			
		}
	}
	
	
	private void configPins(String json){
		JSONObject obj= new JSONObject(json);
		JSONArray ja = obj.getJSONArray("pins");
		for (int i = 0; i < ja.length(); i++) {
		    JSONObject x = ja.getJSONObject(i);
		    String pinNumber = String.valueOf(x.getInt("pinNumber"));
		    if(x.getBoolean("pinType") == true) {
		    	if(outputs.containsKey(pinNumber) == false) {
		    		if(inputs.containsKey(pinNumber)) {
		    			inputs.remove(String.valueOf(pinNumber));
			    	}
		    			outputs.put( String.valueOf(pinNumber) ,gpio.provisionDigitalOutputPin(RaspiPin.getPinByAddress(Integer.valueOf(pinNumber)), "OutPin_"+pinNumber, PinState.getState(!x.getBoolean("pinState"))));
		    	} 	
		    }else {
		    	if(inputs.containsKey(pinNumber) == false) {
		    		if(outputs.containsKey(pinNumber)) {
		    			outputs.remove(String.valueOf(pinNumber));
			    	}
		    		inputs.put( String.valueOf(pinNumber) ,gpio.provisionDigitalInputPin(RaspiPin.getPinByAddress(Integer.valueOf(pinNumber)), "InPin_"+pinNumber, PinPullResistance.valueOf("PULL_DOWN")));
		    	}
		    
		    }
		}
		
		for(Listener listener : listeners){
			listener.pinsConfigured();
		}
		this.addListeners(tcp);
		
	}
	
	
	public void setTcpListeners(Tcp tcp) {
		this.tcp = tcp;
	}
	
	public void addListener(Listener listener){
		this.listeners.add(listener);
	}
	
	public static void addListeners(Tcp tcp){
		inputListeners = new ArrayList<GpioPinDigitalInput>(inputs.values());
		for(GpioPinDigitalInput gpio : inputListeners){
			gpio.addListener(tcp);
		}
	}

	public void changePinState(String pinNumber){
			outputs.get(pinNumber).toggle();
	}
	
}






















