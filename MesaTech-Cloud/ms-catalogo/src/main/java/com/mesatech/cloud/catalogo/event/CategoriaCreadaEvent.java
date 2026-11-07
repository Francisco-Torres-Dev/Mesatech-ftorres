package com.mesatech.cloud.catalogo.event;

import java.io.Serializable;

public record CategoriaCreadaEvent(
    String eventId,
    String tipoEvento,
    String fechaEvento,
    Long categoriaId,
    String nombre
) implements Serializable {}
