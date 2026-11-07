package com.mesatech.cloud.solicitudes.event;

import java.io.Serializable;

public record CategoriaCreadaEvent(
    String eventId,
    String tipoEvento,
    String fechaEvento,
    Long categoriaId,
    String nombre
) implements Serializable {}
