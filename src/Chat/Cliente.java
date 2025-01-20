package Chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Cliente {

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

	/**
     * Método principal que gestiona la conexión del cliente con el servidor de chat.
     *
     * @param args argumentos de la línea de comandos (no se utilizan).
     */
	public static void main(String[] args) {
		try(
			Socket socket = new Socket("localhost", 5000);
			BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
	        BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));				
		) {
			
	
			
			String mensaje;
			while (!(mensaje = entrada.readLine()).equals("End")) {
				System.out.println(mensaje);
			}	
			
			
		//Seleción canal
			System.out.print("Selecciona un canal: ");
			String seleccionCanal = teclado.readLine();
			salida.println(seleccionCanal);
			
			String respuestaCanal = entrada.readLine();
			System.out.println("SERVER >>> " + respuestaCanal);
			
		//Selección nombre de usuario
			
			System.out.println("Indica nombre de usuario: ");
			String nombreUsuario = teclado.readLine();
			salida.println(nombreUsuario);
			
			String respuestaUsuario = entrada.readLine();
			System.out.println("SERVER >>> " + respuestaUsuario);
			
		//bucle para cuando el nombre es inválido
            while (!respuestaUsuario.equals("OK")) {
                System.out.println("Indica otro nombre de usuario: ");
                nombreUsuario = teclado.readLine();
                salida.println(nombreUsuario);
                respuestaUsuario = entrada.readLine();
                System.out.println("SERVER >>> " + respuestaUsuario);
            }
			
    	//Lanzamiento de hilocliente para recibir mensajes
			Thread hiloRecepcion = new Thread(new HiloClienteRecepcion(entrada));
			hiloRecepcion.start();            
			
		//Bucle para enviar mensajes
			System.out.println("Conexion iniciada, ya puedes enviar mensajes");
			while (true) {
				System.out.print(">");
				mensaje = teclado.readLine();
				String timestamp = LocalDateTime.now().format(formatter);
				System.out.println("[" + timestamp +"]" + nombreUsuario + ">>>" + mensaje);
				salida.println(mensaje);
			}
			
		} catch (Exception e) {
			System.out.println("CLIENT >>> ERROR de conexión con el servidor");
			e.printStackTrace();
		}

	}

}
