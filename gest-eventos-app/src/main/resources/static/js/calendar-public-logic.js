// ===========================================================
// 1. CONFIGURACIÓN GLOBAL
// ===========================================================

const ANGULAR_ORIGIN = "http://localhost:4200"; // TODO: cambiar a producción cuando se despliegue

// ===========================================================
// 2. FUNCIONES HELPERS (Para el manejo de Fechas)
// ===========================================================

/**
 * Formatea fechas de inicio y fin para mejorar la UX y el flujo de reservas.
 * @param {string} startStr - Fecha y hora de inicio del slot.
 * @param {string} endStr - Fecha y hora de fin del slot.
 * @returns {object} { formattedStart, formattedEnd }
 */
function formatSlotTimes(startStr, endStr) {
  const options = {
    weekday: "long",
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  };

  const startDate = new Date(startStr);
  const endDate = new Date(endStr);

  const formattedStart = startDate.toLocaleString("es-ES", options);
  const formattedEnd = endDate.toLocaleTimeString("es-ES", {
    hour: "2-digit",
    minute: "2-digit",
  });

  return { formattedStart, formattedEnd };
}

/**
 * Convierte un objeto Date de JS a una cadena ISO 8601 local (YYYY-MM-DDTHH:mm:ss.SSS)
 * @param {Date} date
 * @returns {string} Cadena de fecha y hora local.
 */
function toLocalISOString(date) {
  // 1. Calcula el offset y lo resta para obtener la fecha/hora local "como si fuera UTC".
  const dateWithOffset = new Date(
    date.getTime() - date.getTimezoneOffset() * 60000
  );

  // 2. Usamos toISOString(), cortamos la 'Z' final, y mantenemos los milisegundos si los hay
  // Ejemplo de salida: "2025-11-25T12:30:00.000" (Longitud 23)
  return dateWithOffset.toISOString().slice(0, 23);
}

/**
 * Calcula la duración de 'snap' de FullCalendar en formato HH:mm:ss.
 * @param {number} duracionMinutos
 * @returns {string} Duración en formato HH:mm:ss
 */
function calculateSnapDuration(duracionMinutos) {
  if (duracionMinutos >= 60 && duracionMinutos % 60 === 0) {
    const horas = duracionMinutos / 60;
    return `${String(horas).padStart(2, "0")}:00:00`;
  } else {
    return `00:${String(duracionMinutos).padStart(2, "0")}:00`;
  }
}

// ==========================================================================
// 2.1 HELPERS DE VALIDACIÓN (Para organizar la lógica del evento 'select')
// ==========================================================================

/**
 * Valida que la hora de finalización del slot no exceda la hora de cierre del servicio.
 */
function validateClosingTime(start, endMs, horaCierre) {
  const [cierreHour, cierreMin] = horaCierre.split(":").map(Number);
  const fechaCierre = new Date(start);
  fechaCierre.setHours(cierreHour, cierreMin, 0, 0);

  if (endMs > fechaCierre.getTime()) {
    Swal.fire({
      title: "Horario no permitido",
      text: `El servicio excede la hora de cierre (${horaCierre}).`,
      icon: "warning",
    });
    return false;
  }
  return true;
}

/**
 * Valida que el inicio del slot no esté en el pasado.
 */
function validateCurrentTime(startMs) {
  const nowMs = new Date().getTime();
  if (startMs <= nowMs) {
    Swal.fire({
      title: "Tiempo Expirado",
      text: "El horario seleccionado ya ha comenzado y no se puede reservar.",
      icon: "error",
      confirmButtonText: "Entendido",
    });
    return false;
  }
  return true;
}

// TODO: PRODUCCION Quitar Debugs
/**
 * Valida que el slot no se solape con eventos existentes (reservas).
 */
function validateCollisions(startMs, endMs, calendar) {
  const eventos = calendar.getEvents();
  console.log(`[EVENTOS] Total eventos recogidos: ${eventos.length}`);
  let collisionDetected = false;

  const overlap = eventos.some((ev, index) => {
    // Excluir eventos sin final o de tipo 'background'
    if (ev.display === "background" || !ev.end) {
      if (!ev.end)
        console.warn(
          `[EVENTOS] Evento #${index + 1} (ID: ${
            ev.id || "N/A"
          }) descartado: ev.end es NULL.`
        );
      return false;
    }

    const evStartMs = ev.start.getTime();
    const evEndMs = ev.end.getTime();

    // Lógica estricta de solapamiento
    const isOverlap = startMs < evEndMs && endMs > evStartMs;

    if (isOverlap) {
      console.log("!!! COLISIÓN DETECTADA !!!");
      collisionDetected = true;
    }
    return isOverlap;
  });

  if (overlap) {
    Swal.fire({
      title: "No disponible",
      text: "El hueco seleccionado se solapa con otra reserva.",
      icon: "error",
      confirmButtonText: "Entendido",
    });
    return false;
  }

  // Usamos la bandera para el logging final, como lo hacías antes.
  if (!collisionDetected) {
    console.log("Validación de Colisión: PASADA."); 
  }
  
  return true;
}

/**
 * Lógica para deshabilitar días pasados.
 */
function disablePastDays(info) {
  const hoy = new Date();
  hoy.setHours(0, 0, 0, 0);

  const fechaCelda = new Date(info.date);
  fechaCelda.setHours(0, 0, 0, 0);

  if (fechaCelda < hoy) {
    info.el.classList.add("fc-day-disabled");
  }
}

// ===========================================================
// 3. Funciones para el envío de datos
// ===========================================================
/**
 * Solicita al padre que abra el formulario Angular y espera la respuesta.
 * @param {string} startStr
 * @param {string} endStr
 * @param {number} duracionMinutos
 * @param {string} idServicio
 * @returns {Promise<object|null>} Datos del cliente o null si cancela
 */
function getClientDataFromForm(startStr, endStr, duracionMinutos, idServicio) {
  const PARENT_ORIGIN =
    window.parent === window ? window.location.origin : "http://localhost:8081"; // TODO: CAMBIAR A URL DE PRODUCCION
  const preReserva = {
    startStr,
    endStr,
    formattedStart: formatSlotTimes(startStr, endStr).formattedStart,
    formattedEnd: formatSlotTimes(startStr, endStr).formattedEnd,
    duracionMinutos,
    idServicio,
  };

  return new Promise((resolve) => {
    function onParentMessage(ev) {
      const msg = ev.data;
      if (!msg) return;

      if (msg.type === "clienteData") {
        window.removeEventListener("message", onParentMessage);
        resolve(msg.data);
      } else if (msg.type === "cancel") {
        window.removeEventListener("message", onParentMessage);
        resolve(null);
      }
    }

    window.addEventListener("message", onParentMessage);

    try {
      window.parent.postMessage(
        { type: "openClientForm", data: preReserva },
        PARENT_ORIGIN
      );
    } catch (e) {
      console.error("[CALENDAR] Error enviando openClientForm al padre", e);
      window.removeEventListener("message", onParentMessage);
      resolve(null);
    }
  });
}

/**
 * Ensambla el DTO y envía la solicitud POST al backend.
 * Gestiona errores y notificaciones al usuario mediante SweetAlert2
 * @param {string} idServicio
 * @param {string} startStr
 * @param {string} endStr
 * @param {object} clientData
 */
async function sendPreReservaRequest(idServicio, startStr, endStr, clientData) {
  const reservaRequestDTO = {
    idServicio,
    fechaInicio: startStr,
    fechaFin: endStr,
    nombreCliente: clientData.nombreCliente,
    correoElec: clientData.correoElec,
    telf: clientData.telf,
  };

  try {
    const response = await fetch("/public/reservas/crear", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(reservaRequestDTO),
    });

    const responseText = await response.text();

    if (response.status === 202) {
      Swal.fire({
        title: "Reserva pendiente!",
        text: "Revisa tu correo para confirmar la cita. De lo contrario, en 30 min se cancelará.",
        icon: "success",
        confirmButtonText: "¡Entendido!",
      });
    } else {
      Swal.fire({
        title: "¡Error!",
        text: `Error al crear la pre-reserva. Causa: ${responseText}`,
        icon: "error",
        confirmButtonText: "¡Entendido!",
      });
    }
  } catch (error) {
    console.error("Fallo de red al enviar la reserva:", error);
    Swal.fire({
      title: "¡Error!",
      text: "Error de conexión. Inténtalo de nuevo más tarde.",
      icon: "error",
      position: "top-end",
      timer: 2000,
      toast: true,
      showConfirmButton: false,
    });
  }
}

// ===========================================================
// 4. FUNCIONES DE CALENDARIO (Orquestadores)
// ===========================================================

/**
 * Gestiona el evento 'select' del calendario, realizando todas las validaciones
 * y ejecutando el flujo de pre-reserva (incluyendo la llamada a Angular).
 *
 * @param {object} info - El objeto de información de selección de FullCalendar.
 * @param {object} config - La configuración del servicio (duracionMinutos, horaCierre, idServicio).
 * @param {FullCalendar.Calendar} calendar - Instancia del calendario.
 * @param {function} toLocalISOString - Función helper para formatear fechas.
 */
async function handleSlotSelection(info, config, calendar, toLocalISOString) {
  const { duracionMinutos, horaCierre, idServicio } = config;
  const { start } = info;

  const startMs = start.getTime();
  const end = new Date(startMs + duracionMinutos * 60000);
  const endMs = end.getTime();

  // --- DEBUG ---
  console.groupCollapsed(`[RESERVA] Intento: ${start.toLocaleTimeString()} - ${end.toLocaleTimeString()} (${duracionMinutos} min)`);
  console.log(`Inicio (Local Madrid): ${toLocalISOString(start)}`);
  console.log(`Fin Target (UTC): ${end.toISOString()}`);

  try {
    // --- VALIDACIÓN A: HORA DE CIERRE ---
    if (!validateClosingTime(start, endMs, horaCierre)) {
      calendar.unselect();
      return;
    }

    // --- VALIDACIÓN B: HORA ACTUAL (NOW) ---
    if (!validateCurrentTime(startMs)) {
      calendar.unselect();
      return;
    }

    // --- VALIDACIÓN C: COLISIONES (Solapamiento) ---
    if (!validateCollisions(startMs, endMs, calendar)) { // <--- Esta función debe incluir el logging corregido
      calendar.unselect();
      return;
    }

    // --- AJUSTE AUTOMÁTICO (Snap) con Arreglo de Rendering ---
    const diff = Math.abs(info.end.getTime() - endMs);
    if (diff > 1000) {
      calendar.unselect();
      setTimeout(() => calendar.select(start, end), 10);
      return;
    }

    // --- FLUJO DE RESERVA (COMUNICACIÓN EXTERNA) ---

    // Preparamos las fechas en el formato exacto que el Backend espera (sin 'Z')
    const startLocalStr = toLocalISOString(start);
    const endLocalStr = toLocalISOString(end);

    // Llama al formulario Angular y espera los datos del cliente
    const clientData = await getClientDataFromForm(
      startLocalStr,
      endLocalStr,
      duracionMinutos,
      idServicio
    );

    if (!clientData) {
      Swal.fire({
        title: "Reserva Cancelada",
        text: "Debes completar los datos para reservar.",
        icon: "info",
        timer: 2000,
        showConfirmButton: false,
      });
      return; // No unselect aquí, se hace en el finally
    }

    // --- Enviar pre-reserva al Backend ---
    await sendPreReservaRequest(
      idServicio,
      startLocalStr,
      endLocalStr,
      clientData
    );

  } catch (error) {
    console.error("Error crítico durante la selección del slot:", error);
    Swal.fire({
      title: "Error Inesperado",
      text: "Ocurrió un error al procesar la reserva. Revise la consola.",
      icon: "error",
    });
  } finally {
    calendar.unselect();
    console.groupEnd();
  }
}

/**
 * Inicializa el calendario a partir de la configuración obtenida del backend
 * @param {object} config
 */
function initCalendar(config) {
  const calendarEl = document.getElementById("calendar");
  if (!calendarEl) {
    console.error("Contenedor #calendar no encontrado. Abortando.");
    return;
  }

  const calendar = createCalendar(calendarEl, config);
  calendar.render();
}

/**
 * Crea y devuelve una instancia de FullCalendar con toda la configuración necesaria
 * @param {HTMLElement} calendarEl
 * @param {object} config
 * @returns {FullCalendar.Calendar}
 */
function createCalendar(calendarEl, config) {
  const {
    idServicio,
    duracionMinutos,
    horaApertura,
    horaCierre,
    diasApertura,
  } = config;

  // Preparación de datos de configuración
  const diasAperturaArray = diasApertura
    ? diasApertura.split(",").map((n) => {
        const num = Number(n);
        return num === 7 ? 0 : num;
      })
    : [];

  // Se asume que calculateSnapDuration es un helper ahora
  const slotDurationString = calculateSnapDuration(duracionMinutos); 

  const calendar = new FullCalendar.Calendar(calendarEl, {
    // --- CONFIGURACIÓN ESTÁTICA ---
    themeSystem: "bootstrap5",
    locale: "es",
    timeZone: "local",
    forceEventDuration: true,
    initialView: "timeGridWeek",
    allDaySlot: false,
    selectOverlap: false,
    nowIndicator: true,
    editable: false,
    selectable: true,
    selectMirror: true,
    expandRows: true,
    
    // --- HORARIOS Y VISTA ---
    slotMinTime: horaApertura,
    slotMaxTime: horaCierre,
    slotDuration: "00:15:00",
    snapDuration: slotDurationString,
    slotLabelFormat: { hour: "2-digit", minute: "2-digit", meridiem: false },
    businessHours: {
      daysOfWeek: diasAperturaArray,
      startTime: horaApertura,
      endTime: horaCierre,
    },
    scrollTime: horaApertura,
    headerToolbar: {
      left: "prev,next today",
      center: "title",
      end: "dayGridMonth,timeGridWeek,timeGridDay",
    },
    
    // --- DATOS Y RENDERING ---
    events: "/public/api/calendario/eventos/" + idServicio,
    eventDidMount: function (info) {
      // Oculta informacion del evento
      const titleEl = info.el.querySelector(".fc-event-title");
      if (titleEl) titleEl.style.display = "none";
    },

    // --- HOOKS DE FULLCALENDAR ---
    dayCellDidMount: disablePastDays,
    
    selectAllow: (selectInfo) => {
      // Lógica de validación de días de apertura y pasado
      const day = selectInfo.start.getDay();
      const hoy = new Date();
      hoy.setHours(0, 0, 0, 0);

      const fechaSeleccionada = new Date(selectInfo.start);
      fechaSeleccionada.setHours(0, 0, 0, 0);

      if (!diasAperturaArray.includes(day)) return false;
      if (fechaSeleccionada < hoy) return false;

      return true;
    },
    
    // --- MANEJADOR DE EVENTO PRINCIPAL ---
    select: (info) => {
      // Llama al manejador de flujo, delegando toda la lógica
      handleSlotSelection(info, config, calendar, toLocalISOString);
    },
    
    eventClick: (info) => {
      Swal.fire({
        title: "Error!",
        text: "Este turno ya está ocupado.",
        icon: "error",
        confirmButtonText: "Entendido!",
      });
    },
  });
  
  return calendar;
}

// ===========================================================
// 5. CARGA DE CONFIGURACIÓN DESDE EL BACKEND
// ===========================================================

async function loadCalendarConfiguration() {
  const pathSegments = window.location.pathname.split("/").filter(Boolean);
  const idServicioParaAPI = pathSegments[pathSegments.length - 1] || null;

  if (!idServicioParaAPI || isNaN(idServicioParaAPI)) {
    const calendarEl = document.getElementById("calendar");
    if (calendarEl) {
      calendarEl.innerHTML =
        '<div class="alert alert-danger">No se pudo identificar el servicio a reservar.</div>';
    }
    console.error("ID de servicio inválido:", idServicioParaAPI);
    return;
  }

  try {
    const response = await fetch(
      "/public/api/calendario/config/" + idServicioParaAPI
    );
    if (!response.ok) throw new Error(`Error ${response.status}`);
    const config = await response.json();
    initCalendar(config);
  } catch (error) {
    console.error("Error al cargar configuración:", error);
    const calendarEl = document.getElementById("calendar");
    if (calendarEl) {
      calendarEl.innerHTML =
        '<div class="alert alert-danger">No se pudo cargar la disponibilidad del servicio.</div>';
    }
  }
}

// ===========================================================
// 6. INICIALIZACIÓN AL CARGAR EL DOM
// ===========================================================

document.addEventListener("DOMContentLoaded", loadCalendarConfiguration);
