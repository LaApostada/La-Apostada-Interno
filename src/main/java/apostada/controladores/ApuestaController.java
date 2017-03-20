package apostada.controladores;

import apostada.entidades.Partido;
import apostada.servicios.ApuestaService;
import apostada.servicios.PartidoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import apostada.entidades.Apuesta;
import apostada.entidades.Usuario;
import apostada.servicios.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
public class ApuestaController {
	
	@Autowired
	private PartidoService partidoService;
	
	@Autowired
	private ApuestaService apuestaService;
	
	@Autowired
	private UsuarioService usuarioService;
	
	@PostMapping(value="/apuesta")
	public ResponseEntity<Object> apostar(@RequestBody Apuesta apuesta) {
		// Validar usuario y partido
		if (apuesta == null
				|| apuesta.getPartido() == null
				|| apuesta.getUsuario() == null
				|| partidoService.findById(apuesta.getPartido().getId()) == null
				|| usuarioService.findById(apuesta.getUsuario().getId()) == null) {	
			return new ResponseEntity<>("Apuesta, partido o usuario no validos", HttpStatus.NOT_FOUND);
		} else {
			Usuario usuario = apuesta.getUsuario();
			Partido partido = apuesta.getPartido();
			
			// Cantidad
			if (usuario.getPuntos() < apuesta.getCantidadApostada()) {
				return new ResponseEntity<>("No tienes los puntos suficientes", HttpStatus.BAD_REQUEST);
			} else if (apuesta.getCantidadApostada() <= 0) {
				return new ResponseEntity<>("Cantidad tiene que ser mayor de 0", HttpStatus.BAD_REQUEST);
			} else {
				double coutaOriginal;
				
				// Resultado
				switch (apuesta.getResultado()) {
					case Apuesta.RESULTADO_VICTORIA_LOCAL:
						coutaOriginal = partido.getCuotaLocal();
						break;
					case Apuesta.RESULTADO_EMPATE:
						coutaOriginal = partido.getCuotaEmpate();
						break;
					case Apuesta.RESULTADO_VICTORIA_VISITANTE:
						coutaOriginal = partido.getCuotaVisitante();
						break;
					default:
						return new ResponseEntity<>("Resultado incorrecto", HttpStatus.BAD_REQUEST);
				}
				
				// Cuota
				if (coutaOriginal != apuesta.getCuota()) {
					return new ResponseEntity<>("Lo sentimos, la cuota ha sido actualizada. Intentalo otra vez", HttpStatus.BAD_REQUEST);
				} else {
					// Hacer apuesta
					usuario.restarPuntos(apuesta.getCantidadApostada());
					partido.ajusteCuota(apuesta.getCantidadApostada(), apuesta.getResultado());
					partidoService.save(partido);
					apuestaService.save(apuesta);

					return new ResponseEntity<>(apuesta, HttpStatus.OK);
				}
			}
		}
	}
	
}
