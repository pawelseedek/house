package com.packt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pinout.InOut;

public class Tcp implements Connection.Listener, GpioPinListenerDigital, Gpio.Listener {

    
//	private String address = "192.168.1.16";
	 private String address = "54.37.137.86";
	private int port = 4444;
	private Socket socket = new Socket();
	private TcpConnection connection;
	private InOut gpio;
	private String houseid;
	private String password;
	private Immutable immutable = new Immutable();
	
	private SynchronizedLinkedList kolejka;
	
	public Tcp(String houseid, String password, InOut gpio, SynchronizedLinkedList kolejka ) {
		try {
			socket.connect(new InetSocketAddress(address, port), 10000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		connection = new TcpConnection(socket);
		connection.addListener(this);
		connection.start();
		
		this.gpio = gpio;
		this.houseid = houseid;
		this.password = password;
		connection.send((immutable.HOUSEID+"."+houseid +"."+password).getBytes());
		
		this.kolejka = kolejka;
		gpio.addListener(this);
	}
	

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        String pinNumber = event.getPin().toString().split("O")[1].substring(1, 3);
		connection.send(pinNumber.getBytes());
    }
	
	@Override
	public void pinChanged(String pinNumber){
		connection.send(pinNumber.getBytes());
	}

	@Override
	public void pinsConfigured() {
		connection.send(null);
	}
	
	@Override
	public void messageReceived (Connection connection, Object message){
		byte[] b = (byte[]) message;
		String msg = new String(b);
			kolejka.add(msg);
	}
	
	@Override
    public void connected(Connection connection) {
		
	}
	
	@Override
    public void disconnected(Connection connection) {
		
	}
	
	public TcpConnection getConnection() {
		return this.connection;
	}
    
	
	public void setHouseId(String houseid){
		this.houseid = houseid;
	}


}
