package game.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ConexionServer extends Conexion {
	
	public ConexionServer() {
		try {
			this.setSocket(new DatagramSocket(1234));
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void conectar() {
		setConectados(true);
		DatagramPacket packet = recibirPaquete();
		String msg = new String(packet.getData()).trim();
		
		if (msg.equals("start")) {
			Enviar("start");
		}
		else
			setConectados(false);
		

		if (conectados())
		System.out.println("Conectado al cliente");
	}
}
