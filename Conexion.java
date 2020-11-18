package game.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public abstract class Conexion {
	private DatagramSocket socket;
	private boolean conectados = false;
	private InetAddress direccion;
	private DatagramPacket unPaquete;

		
	public Conexion() {
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public abstract void conectar();
	
	public DatagramSocket getSocket() {
		return socket;
	}
	
	public void setSocket(DatagramSocket datagramSocket) {
		this.socket = datagramSocket;
	}
	
	public boolean conectados() {
		return conectados;
	}
	
	public void setConectados(boolean conectados) {
		this.conectados = conectados;
	}
	
	
	
	public InetAddress getDireccion() {
		return direccion;
	}

	public void setDireccion(InetAddress direccion) {
		this.direccion = direccion;
	}

	public void Enviar (String msg) {
		synchronized (getSocket()) {
			byte[] msgBytes = msg.getBytes();
			DatagramPacket packet = null;
			setConectados(true);
			
			if (getDireccion() != null)
				packet  = new DatagramPacket(msgBytes, msgBytes.length, getDireccion(), 1234);
			else {
				System.out.println(getUnPaquete().getPort());
				packet  = new DatagramPacket(msgBytes, msgBytes.length, unPaquete.getAddress(), getUnPaquete().getPort());
			}
			
			try {
				getSocket().send(packet);
			} catch (IOException e) {
				e.printStackTrace();
				setConectados(false);
			}
		}
	}
	
	public String recibir(){
		synchronized (getSocket()) {
			byte[] buffer = new byte[256];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			setUnPaquete(packet);
			try {
				getSocket().setSoTimeout(0);
				getSocket().receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return new String(packet.getData()).trim();
		}
	}
	
	public String recibir(int timeout) throws SocketTimeoutException {
		synchronized (getSocket()) {
			byte[] buffer = new byte[256];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			setUnPaquete(packet);
			try {
				getSocket().setSoTimeout(timeout);
				getSocket().receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return new String(packet.getData()).trim();
		}
	}
	
	public DatagramPacket recibirPaquete() {
		synchronized (getSocket()) {
			byte[] buffer = new byte[256];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			setUnPaquete(packet);
			try {
				getSocket().setSoTimeout(0);
				getSocket().receive(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return packet;
		}
	}
	
	public void setUnPaquete(DatagramPacket packet) {
		this.unPaquete = packet;
	}
	
	public DatagramPacket getUnPaquete() {
		return this.unPaquete;
	}
}
