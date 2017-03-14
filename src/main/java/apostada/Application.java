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
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;



@SpringBootApplication
public class Application extends AsyncConfigurerSupport  {

	/*public static void main(String[] args) throws ClassNotFoundException {
		SpringApplication.run(Application.class, args);
		
		
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
	}*/
	
	public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(Application.class)
                .web(false)
                .run(args);
        int port = context.getBean(TcpServerConnectionFactory.class).getPort();
		
		ServerSocketFactory serverSocketFactory = SSLServerSocketFactory.getDefault();
		SSLServerSocket serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(port);
		while (true) {
			Socket socket = serverSocket.accept();
			Thread t = new Thread(new ProcesadorEco(socket));
			t.start();
		}
		
        Socket socket = SocketFactory.getDefault().createSocket("localhost", port);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line = reader.readLine();
        System.out.println(line);
        context.close();
		
		//El puerto de la página
		int puerto = 8888;
		try {
			ServerSocket serverSocket = new ServerSocket(puerto);
			Socket laapostada = serverSocket.accept();
			ObjectInputStream in = new ObjectInputStream(laapostada.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(laapostada.getOutputStream());
	
			while (true) {
				 Apuesta apuesta = (Apuesta)in.readObject();
				 Partido partido = apuesta.getPartido();
				 partido.ajusteCuota(apuesta.getCantidadApostada(), apuesta.getResultado());
				 partidoService.save(partido);
				 apuestaService.save(apuesta);
				 out.writeObject(partido);
			}
		} catch(IOException e) {
			System.out.println("Fallo en la conexión");
		}
    }

    @Bean
    public TcpReceivingChannelAdapter server(TcpNetServerConnectionFactory cf) {
        TcpReceivingChannelAdapter adapter = new TcpReceivingChannelAdapter();
        adapter.setConnectionFactory(cf);
        adapter.setOutputChannel(inputChannel());
        return adapter;
    }

    @Bean
    public MessageChannel inputChannel() {
        return new QueueChannel();
    }

    @Bean
    public MessageChannel outputChannel() {
        return new DirectChannel();
    }

    @Bean
    public TcpNetServerConnectionFactory cf() {
        return new TcpNetServerConnectionFactory(0);
    }

    @Bean
    public IntegrationFlow outbound() {
        return IntegrationFlows.from(outputChannel())
                .handle(sender())
                .get();
    }

    @Bean
    public MessageHandler sender() {
        TcpSendingMessageHandler tcpSendingMessageHandler = new TcpSendingMessageHandler();
        tcpSendingMessageHandler.setConnectionFactory(cf());
        return tcpSendingMessageHandler;
    }

    @Bean
    public ApplicationListener<TcpConnectionOpenEvent> listener() {
        return new ApplicationListener<TcpConnectionOpenEvent>() {
            @Override
            public void onApplicationEvent(TcpConnectionOpenEvent event) {
                outputChannel().send(MessageBuilder.withPayload("foo")
                        .setHeader(IpHeaders.CONNECTION_ID, event.getConnectionId())
                        .build());
            }
        };
    }

}
