package internoRest;

import apostada.entidades.Partido;
import apostada.servicios.ApuestaService;
import apostada.servicios.PartidoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import apostada.entidades.Apuesta;

@RestController
public class InternoRest {
	@PutMapping(value="/apostar")
	public ResponseEntity<Partido> actualizarCuota(@RequestBody Apuesta apuesta){
		
		Partido p = apuesta.getPartido();
		p.ajusteCuota(apuesta.getCantidadApostada(), apuesta.getResultado());
		
		PartidoService ps = new PartidoService();
		ApuestaService as = new ApuestaService();
		
		if(ps.findById(p.getId())!=null){
			ps.save(p);
			as.save(apuesta);
			return new ResponseEntity<>(p, HttpStatus.OK);}
		else{
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
}
