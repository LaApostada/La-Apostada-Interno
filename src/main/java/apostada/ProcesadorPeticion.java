package apostada;

import apostada.entidades.Apuesta;
import apostada.entidades.Partido;
import apostada.messages.TestDTO;
import apostada.servicios.ApuestaService;
import apostada.servicios.PartidoService;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcesadorPeticion implements Runnable {

	private final Socket socket;
	
	@Autowired
	private PartidoService partidoService;
	@Autowired
	private ApuestaService apuestaService;
	
	public ProcesadorPeticion(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		System.out.println("=> Nueva peticion aceptada");
		
		try {
			ObjectInputStream in = new ObjectInputStream(this.socket.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(this.socket.getOutputStream());
			
			Object newObject;
			while ((newObject = in.readObject()) != null) {
				System.out.println("=> Nuevo objeto recibido");
				
				if (newObject instanceof Apuesta) {
					System.out.println("=> Nueva apuesta recibida");
					
					// Procesar nueva apuesta
					Apuesta apuesta = (Apuesta) newObject;
					Partido partido = apuesta.getPartido();

					// Realizar calculo
					partido.ajusteCuota(apuesta.getCantidadApostada(), apuesta.getResultado());
					partidoService.save(partido);
					apuestaService.save(apuesta);
					out.writeObject(partido);
				} else if (newObject instanceof TestDTO) {
					System.out.println("HOLA: " + ((TestDTO) newObject).name);
				} else {
					System.out.println("Objeto desconocido");
				}
			}
		} catch (IOException ex) {
			System.out.println("Fallo en la conexión");
		} catch (ClassNotFoundException ex) {
			System.out.println("Fallo en la conversion del objeto");
		}
	}
	
}
