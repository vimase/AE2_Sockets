package Chat;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class HiloServidor implements Runnable{
	
	Socket cliente;
	
	public HiloServidor(Socket cliente) {
		this.cliente = cliente;
	}
	
	/**
     * Lee la lista de canales disponibles desde un archivo.
     *
     * @return una lista de nombres de canales.
     * @throws FileNotFoundException si el archivo "canales.txt" no se encuentra.
     * @throws IOException si ocurre un error al leer el archivo.
     */
	private List<String> leerCanales() throws FileNotFoundException, IOException{
		List<String> canales = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader("canales.txt"))) {
			String linea;
			while((linea = br.readLine()) != null) {
				canales.add(linea);
			}
		} 
		return canales;
	}
	
	/**
     * Método que ejecuta el hilo para gestionar la interacción con el cliente.
     */
	@Override
	public void run() {
		try(
			BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
			PrintWriter salida = new PrintWriter(cliente.getOutputStream(), true);
					
		) {
			List<String> canales = leerCanales();
			
			salida.println("Canales disponibles: ");
			for(String canal : canales) {
				salida.println(canal);
			}
			salida.println("End");	
			
		//Recepcion de seleccion de canal del cliente en el servidor
			String seleccionCanal = entrada.readLine();
			System.out.println("SERVER >>> Cliente ha seleccionado el canal: " + seleccionCanal);
			
			if( seleccionCanal.matches("[1-4]")) {
				salida.println("Canal seleccionado: " + seleccionCanal);
			} else {
				salida.println("Selección inválida");
				return;
			}
			
		//Nombre usuario
			String nombreUsuario = entrada.readLine();	
			while(true) {
				if(nombreUsuario.contains(" ") || nombreUsuario == null) {
					salida.println("Nombre de usuario inválido, no puede tener espacios");
					continue;
				}
					
				synchronized (Servidor.participantesPorCanal) {
					Servidor.participantesPorCanal.putIfAbsent(seleccionCanal, new ArrayList<>());
					if(Servidor.participantesPorCanal.get(seleccionCanal).contains(nombreUsuario)) {
						salida.println("El nombre de usuario ya existe, indica otro ");
						nombreUsuario = entrada.readLine();
					} else {
						Servidor.participantesPorCanal.get(seleccionCanal).add(nombreUsuario);
						salida.println("OK");
						System.out.println("SERVER >>> Cliente " + nombreUsuario + " registrado en el canal " + seleccionCanal);
						break;	
					}
				}
			}
			
			
			synchronized (Servidor.salidasPorUsuario) {
	            Servidor.salidasPorUsuario.put(nombreUsuario, salida);
	        }
			
		//Bucle recepción de mensajes
			String mensaje;
			while((mensaje = entrada.readLine()) != null) {
				if(mensaje.equalsIgnoreCase("whois")) {
					synchronized (Servidor.participantesPorCanal) {
						List<String> participantes = Servidor.participantesPorCanal.get(seleccionCanal);
						salida.println("Usuarios activos canal " + seleccionCanal + " " + String.join(", ", participantes));
					}
				} else if (mensaje.startsWith("@")) {
					String[] partes = mensaje.split(" ", 2); //Creamos un array en el cual la primera parte del mensaje sera el canal destinatario
					if (partes.length == 2) {
						String canalDestinatario = partes[0].substring(1);
						String contenidoMensaje = partes[1];
						synchronized (Servidor.participantesPorCanal) {
							List<String> usuariosDestinatarios = Servidor.participantesPorCanal.get(canalDestinatario);
							if (usuariosDestinatarios != null) {
								for(String usuario : usuariosDestinatarios) {
									PrintWriter salidaDestinatario = Servidor.salidasPorUsuario.get(usuario);
				                    if (salidaDestinatario != null) {
				                        salidaDestinatario.println("Mensaje en canal " + canalDestinatario + " de " + nombreUsuario + ": " + contenidoMensaje);
				                    }
				                }
				                System.out.println("SERVER >>> Mensaje enviado al canal " + canalDestinatario + " de " + nombreUsuario + ": " + contenidoMensaje);
							} else {
								salida.println("El canal no existe" + canalDestinatario);
							}
						}
					}
				} else if (mensaje.equalsIgnoreCase("exit")) {
					synchronized (Servidor.participantesPorCanal) {
						List<String> participantes = Servidor.participantesPorCanal.get(seleccionCanal);
				        if (participantes != null) {
				            participantes.remove(nombreUsuario);
				        }
					}
					salida.println("Desconectando del servidor");
					break;
				} else if (mensaje.equalsIgnoreCase("channels")) {
					salida.println("Canales disponibles: " + String.join(", ", canales));
				} else {
					synchronized (Servidor.participantesPorCanal) {
						List<String> participantes = Servidor.participantesPorCanal.get(seleccionCanal);
						for (String usuario : participantes) {
	                        if (!usuario.equals(nombreUsuario)) { // Excluir al remitente
	                            PrintWriter salidaDestinatario = Servidor.salidasPorUsuario.get(usuario);
	                            if (salidaDestinatario != null) {
	                                salidaDestinatario.println(nombreUsuario + ": " + mensaje);
	                            }
	                        }
	                    }
					}
				}
				System.out.println("SERVER >>> Mensaje en canal " + seleccionCanal + " de " + nombreUsuario + ": " + mensaje);
			}
			
			
		} catch (IOException e) {
			System.err.println("SERVER >>> ERROR de conexión con el cliente");
			e.printStackTrace();
		}
		
			
		
	}

}
