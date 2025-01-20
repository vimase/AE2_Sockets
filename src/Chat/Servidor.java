package Chat;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Servidor {

	public static Map<String, List<String>> participantesPorCanal = new HashMap<>();	
	public static Map<String, PrintWriter> salidasPorUsuario = new HashMap<>();

	/**
     * Método principal que inicia el servidor.
     * <p>
     * El servidor escucha conexiones entrantes en el puerto 5000. Por cada
     * conexión aceptada, se lanza un hilo para manejar la comunicación con el cliente.
     * </p>
     *
     * @param args argumentos de la línea de comandos (no se utilizan).
     */
	public static void main(String[] args) {
		System.out.println("SERVER >>> Iniciando server");
		ServerSocket socketEscucha = null;
		try {
			socketEscucha = new ServerSocket(5000); //espera conexiones de clientes en el puerto 5000
		}catch (IOException e) {
			System.err.println("SERVER >>> ERROR");
			e.printStackTrace();
			return;
		}
		
		while (true) {
			Socket conexion;
			try {
				conexion = socketEscucha.accept(); //para aceptar las cnexiones de los clientes
				System.out.println("SERVER >>> Conexion recibida --> Lanzando hilo");
				HiloServidor hs = new HiloServidor(conexion);// creamos instancia
				Thread hilo = new Thread(hs); 
				hilo.start(); //lanzamos un hilo para cada cliente (para que varios clientes se conecten al servidor)
				
			} catch (IOException e) {
				System.err.println("SERVER >>> ERROR");
				e.printStackTrace();
			}
		}
	}

}
