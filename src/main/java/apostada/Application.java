package apostada;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;

import apostada.entidades.Apuesta;
import apostada.entidades.Partido;
import apostada.servicios.ApuestaService;
import apostada.servicios.PartidoService;



@SpringBootApplication
public class Application extends AsyncConfigurerSupport  {


	@Autowired
	private static PartidoService partidoService;

	@Autowired
	private static ApuestaService apuestaService;
	
	
	public static void main(String[] args) throws ClassNotFoundException {
		SpringApplication.run(Application.class, args);
		
		//El puerto de la página
		int puerto = 8888;
		try{
			ServerSocket serverSocket = new ServerSocket(puerto);
			Socket laapostada = serverSocket.accept();
			ObjectInputStream in = new ObjectInputStream(laapostada.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(laapostada.getOutputStream());
	
			while(true){
				 Apuesta apuesta = (Apuesta)in.readObject();
				 Partido partido = apuesta.getPartido();
				 partido.ajusteCuota(apuesta.getCantidadApostada(), apuesta.getResultado());
				 partidoService.save(partido);
				 apuestaService.save(apuesta);
				 out.writeObject(partido);
			}
			
		
		
		}
		
		
		catch(IOException e){
			System.out.println("Fallo en la conexión");
		}
	}

	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(2);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("InternalService-");
		executor.initialize();
		return executor;
	}

}
