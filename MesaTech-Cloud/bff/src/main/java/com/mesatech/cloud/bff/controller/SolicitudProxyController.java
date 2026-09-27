package com.mesatech.cloud.bff.controller;

import com.mesatech.cloud.bff.dto.AuthUserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@RestController
@RequestMapping("/v1")
public class SolicitudProxyController {

    private final RestClient restClient;
    private final RestClient catalogoClient;

    public SolicitudProxyController(@Value("${mesatech.api.solicitudes-url:http://localhost:8081}") String solicitudesUrl,
                                   @Value("${mesatech.api.catalogo-url:http://localhost:8082}") String catalogoUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(solicitudesUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.catalogoClient = RestClient.builder()
                .baseUrl(catalogoUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @PostMapping("/solicitudes")
    public Object createSolicitud(@RequestBody Map<String, Object> payload) {
        return restClient.post()
                .uri("/v1/solicitudes")
                .body(payload)
                .retrieve()
                .body(Object.class);
    }

    @GetMapping("/solicitudes")
    public Object getAllSolicitudes() {
        return restClient.get()
                .uri("/v1/solicitudes")
                .retrieve()
                .body(Object.class);
    }

    @GetMapping("/solicitudes/mias")
    public Object getMySolicitudes(@RequestParam String email) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/v1/solicitudes/mias").queryParam("email", email).build())
                .retrieve()
                .body(Object.class);
    }

    @GetMapping("/solicitudes/{id}")
    public Object getSolicitud(@PathVariable Long id) {
        return restClient.get()
                .uri("/v1/solicitudes/{id}", id)
                .retrieve()
                .body(Object.class);
    }

    @PutMapping("/solicitudes/{id}/estado")
    public Object updateEstado(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return restClient.put()
                .uri("/v1/solicitudes/{id}/estado", id)
                .body(payload)
                .retrieve()
                .body(Object.class);
    }

        @PutMapping("/solicitudes/{id}")
        public Object updateSolicitud(@PathVariable Long id,
                      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                      @RequestBody Map<String, Object> payload) {
        return restClient.put()
            .uri("/v1/solicitudes/{id}", id)
            .header(HttpHeaders.AUTHORIZATION, authorization)
            .body(payload)
            .retrieve()
            .body(Object.class);
        }

        @DeleteMapping("/solicitudes/{id}")
        public ResponseEntity<Void> deleteSolicitud(@PathVariable Long id,
                            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        restClient.delete()
            .uri("/v1/solicitudes/{id}", id)
            .header(HttpHeaders.AUTHORIZATION, authorization)
            .retrieve()
            .toBodilessEntity();
        return ResponseEntity.noContent().build();
        }

    @GetMapping("/catalogo")
    public Object getCatalogo() {
        return catalogoClient.get().uri("/v1/catalogo").retrieve().body(Object.class);
    }

    @GetMapping("/catalogo/categorias")
    public Object getCategorias(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return catalogoClient.get()
                .uri("/v1/catalogo/categorias")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(Object.class);
    }

    @PostMapping("/catalogo/categorias")
    public Object createCategoria(@RequestBody Map<String, Object> payload) {
        return catalogoClient.post().uri("/v1/catalogo/categorias").body(payload).retrieve().body(Object.class);
    }

    @PutMapping("/catalogo/categorias/{id}")
    public Object updateCategoria(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return catalogoClient.put().uri("/v1/catalogo/categorias/{id}", id).body(payload).retrieve().body(Object.class);
    }

    @DeleteMapping("/catalogo/categorias/{id}")
    public ResponseEntity<Void> deleteCategoria(@PathVariable Long id) {
        catalogoClient.delete().uri("/v1/catalogo/categorias/{id}", id).retrieve().toBodilessEntity();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/catalogo/prioridades")
    public Object getPrioridades(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return catalogoClient.get()
                .uri("/v1/catalogo/prioridades")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(Object.class);
    }

    @PostMapping("/catalogo/prioridades")
    public Object createPrioridad(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                  @RequestBody Map<String, Object> payload) {
        return catalogoClient.post()
                .uri("/v1/catalogo/prioridades")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(payload)
                .retrieve()
                .body(Object.class);
    }

    @PutMapping("/catalogo/prioridades/{id}")
    public Object updatePrioridad(@PathVariable Long id,
                                  @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                  @RequestBody Map<String, Object> payload) {
        return catalogoClient.put()
                .uri("/v1/catalogo/prioridades/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(payload)
                .retrieve()
                .body(Object.class);
    }

    @DeleteMapping("/catalogo/prioridades/{id}")
    public ResponseEntity<Void> deletePrioridad(@PathVariable Long id,
                                                @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        catalogoClient.delete()
                .uri("/v1/catalogo/prioridades/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .toBodilessEntity();
        return ResponseEntity.noContent().build();
    }
}
