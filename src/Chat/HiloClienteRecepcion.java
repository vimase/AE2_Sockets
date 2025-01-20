package Chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HiloClienteRecepcion implements Runnable {
	private BufferedReader entrada;
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

	public HiloClienteRecepcion(BufferedReader entrada) {
		this.entrada = entrada;
	}
	
	/**
     * Método que ejecuta el hilo para recibir mensajes del servidor.
     */
	@Override
	public void run() {
		try {
			String mensaje;
			while((mensaje = entrada.readLine()) != null) {
				String timestamp = LocalDateTime.now().format(formatter);
				System.err.println("[" + timestamp + "] SERVER >>> " + mensaje);
				
			}
		} catch (IOException e) {
			System.out.println("CLIENT >>> ERROR al recibir mensaje");
			e.printStackTrace();
		}
		
	}
	
}
