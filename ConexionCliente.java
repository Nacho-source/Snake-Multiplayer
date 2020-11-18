package game.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.sun.corba.se.impl.ior.GenericTaggedComponent;

public class ConexionCliente extends Conexion{
	
	public ConexionCliente(String ip) {
		super();
		try {
			this.setDireccion(InetAddress.getByName(ip));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void conectar() {
		Enviar("start");
		
		System.out.println("C: Esperando start");
		String msg = recibir();
		System.out.println("C: Recibido: " + msg);
		System.out.println(msg);
		if (msg.equals("start")) {
			setConectados(true);
		} else {
			setConectados(false);
		}
		
		if (conectados())
			System.out.println("Conectado al server");
	}
}
