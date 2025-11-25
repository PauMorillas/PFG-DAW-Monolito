// ===========================================================
// 1. CONFIGURACIÓN GLOBAL
// ===========================================================

const ANGULAR_ORIGIN = "http://localhost:4200"; // TODO: cambiar a producción cuando se despliegue

// ===========================================================
// 2. HELPER FUNCTIONS
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
// 3. FUNCIONES DE CALENDARIO
// ===========================================================

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

  const diasAperturaArray = diasApertura
    ? diasApertura.split(",").map((n) => {
        const num = Number(n);
        return num === 7 ? 0 : num;
      })
    : [];

  // FullCalendar prefiere el formato 'HH:mm:ss' y puede
  // fallar al interpretar valores de minutos >= 60 (ej: '00:120:00').
  let slotDurationString;
  if (duracionMinutos >= 60 && duracionMinutos % 60 === 0) {
    // Si es un múltiplo exacto de horas (60, 120, 180...)
    const horas = duracionMinutos / 60;
    slotDurationString = `${String(horas).padStart(2, "0")}:00:00`; // Ej salida: "02:00:00"
  } else {
    // Si son minutos arbitrarios (ej: 45, 90) el formato '00:MM:00' es aceptado y correcto.
    slotDurationString = `00:${String(duracionMinutos).padStart(2, "0")}:00`;
  }

  return new FullCalendar.Calendar(calendarEl, {
    themeSystem: "bootstrap5",
    locale: "es",
    timeZone: "local",
    forceEventDuration: true,
    initialView: "timeGridWeek",
    allDaySlot: false,
    nowIndicator: true,
    editable: false,
    selectable: true,
    selectMirror: true,
    expandRows: true,
    slotMinTime: horaApertura,
    slotMaxTime: horaCierre,
    slotDuration: "00:15:00",
    snapDuration: slotDurationString, // ajuste automático al hacer clic
    slotLabelFormat: { hour: "2-digit", minute: "2-digit", meridiem: false },
    businessHours: {
      daysOfWeek: diasAperturaArray,
      startTime: horaApertura,
      endTime: horaCierre,
    },
    scrollTime: horaApertura,
    selectOverlap: false,
    // Deshabilita las fechas del pasado
    dayCellDidMount: function (info) {
      const hoy = new Date();
      hoy.setHours(0, 0, 0, 0);

      const fechaCelda = new Date(info.date);
      fechaCelda.setHours(0, 0, 0, 0);

      if (fechaCelda < hoy) {
        info.el.classList.add("fc-day-disabled");
      }
    },
    headerToolbar: {
      left: "prev,next today",
      center: "title",
      end: "dayGridMonth,timeGridWeek,timeGridDay",
    },
    events: "/public/api/calendario/eventos/" + idServicio,
    eventDidMount: function (info) {
      // Oculta informacion del evento (solo queremos que vea la hora)
      const titleEl = info.el.querySelector(".fc-event-title");
      if (titleEl) titleEl.style.display = "none";
    },
    selectAllow: (selectInfo) => {
      const day = selectInfo.start.getDay();
      const hoy = new Date();
      hoy.setHours(0, 0, 0, 0);

      const fechaSeleccionada = new Date(selectInfo.start);
      fechaSeleccionada.setHours(0, 0, 0, 0);

      if (!diasAperturaArray.includes(day)) return false;
      if (fechaSeleccionada < hoy) return false;

      return true;
    },
    select: async (info) => {
      const calendar = info.view.calendar;
      const start = info.start;
      const startMs = start.getTime(); // Tiempo UTC desde el Epoch (ms)

      // 1. Calcular Fecha Fin OBLIGATORIA
      const end = new Date(startMs + duracionMinutos * 60000);
      const endMs = end.getTime();

      // *** START DEBUG ***
      // Usamos toLocalISOString para que el log muestre la hora de Madrid real (11:30)
      console.groupCollapsed(
        `[RESERVA] Intento: ${start.toLocaleTimeString()} - ${end.toLocaleTimeString()} (Duración: ${duracionMinutos} min)`
      );
      console.log("--- DATOS DEL INTENTO ---");
      console.log(`Inicio (ms/UTC): ${startMs} (${start.toISOString()})`);
      console.log(`Inicio (Local Madrid): ${toLocalISOString(start)}`); // <-- Debug Visual Corregido
      console.log(`Fin Target (ms/UTC): ${endMs} (${end.toISOString()})`);
      console.log("-------------------------");
      // *** END DEBUG ***

      // 🚨 --- VALIDACIÓN A: HORA DE CIERRE ---
      const [cierreHour, cierreMin] = horaCierre.split(":").map(Number);
      const fechaCierre = new Date(start);
      fechaCierre.setHours(cierreHour, cierreMin, 0, 0);

      if (endMs > fechaCierre.getTime()) {
        calendar.unselect();
        Swal.fire({
          title: "Horario no permitido",
          text: `El servicio dura ${duracionMinutos} min y excede la hora de cierre (${horaCierre}).`,
          icon: "warning",
        });
        console.log("❌ Validación de Cierre: FALLADA.");
        console.groupEnd();
        return;
      }

      // ✅ --- VALIDACIÓN C: HORA ACTUAL (NOW) ---
      // CORREGIDO: Usamos new Date() directamente para la comparación UTC.
      const now = new Date();
      const nowMs = now.getTime();

      // Bloquear si el inicio seleccionado es anterior o igual a la hora actual.
      if (startMs <= nowMs) {
        calendar.unselect();
        Swal.fire({
          title: "Tiempo Expirado",
          text: "El horario seleccionado ya ha comenzado y no se puede reservar.",
          icon: "error",
          confirmButtonText: "Entendido",
        });
        console.log(
          "❌ Validación de Hora Actual: FALLADA. Inicio está en el pasado."
        );
        console.groupEnd();
        return;
      }
      console.log("✅ Validación de Hora Actual: PASADA.");

      // 🚧 --- VALIDACIÓN B: COLISIONES (Solapamiento) ---
      let collisionDetected = false;
      const eventos = calendar.getEvents();

      console.log(`[DEBUG EVENTOS] Total eventos recogidos: ${eventos.length}`);

      const overlap = eventos.some((ev, index) => {
        console.log(`[DEBUG EVENTOS] Evaluando evento #${index + 1}`);

        if (ev.display === "background") return false;

        if (!ev.end) {
          console.warn(
            `[DEBUG EVENTOS] Evento #${index + 1} (ID: ${
              ev.id || "N/A"
            }) descartado: ev.end es NULL. ¡Esto debe corregirse en la API!`
          );
          return false;
        }

        const evStartMs = ev.start.getTime();
        const evEndMs = ev.end.getTime();

        // Lógica estricta de solapamiento
        const isOverlap = startMs < evEndMs && endMs > evStartMs;

        // *** START DEBUG EN EL BUCLE ***
        if (isOverlap) {
          console.log("!!! COLISIÓN DETECTADA !!!");
          console.log(
            `Reserva Existente (Local): ${ev.start.toLocaleTimeString()} - ${ev.end.toLocaleTimeString()}`
          );
          console.log(`Inicio Existente (ISO): ${ev.start.toISOString()}`);
          console.log(`Fin Existente (ISO): ${ev.end.toISOString()}`);
          collisionDetected = true;
        }
        // *** END DEBUG EN EL BUCLE ***

        return isOverlap;
      });

      // *** START DEBUG FINAL (DESPUÉS DEL BUCLE) ***
      if (!collisionDetected) {
        console.log("✅ Validación de Colisión: PASADA.");
      }
      console.groupEnd();
      // *** END DEBUG FINAL ***

      if (overlap) {
        calendar.unselect();
        Swal.fire({
          title: "No disponible",
          text: "El hueco seleccionado se solapa con otra reserva.",
          icon: "error",
          confirmButtonText: "Entendido",
        });
        return;
      }

      // --- 3. AJUSTE AUTOMÁTICO (Snap) CON ARREGLO DE RENDERING ---

      // Comprobamos si la selección actual es la deseada (tolerancia de 1 segundo)
      const diff = Math.abs(info.end.getTime() - endMs);

      if (diff > 1000) {
        // 1. Limpiamos la selección actual del slot pequeño
        calendar.unselect();

        // 2. Usamos setTimeout para romper el ciclo y aplicar la selección correcta
        setTimeout(() => {
          calendar.select(start, end);
        }, 10);

        return; // Detenemos la ejecución actual.
      }

      // =======================================================
      // 4. FLUJO DE RESERVA (Solo se ejecuta cuando el snap ya está completo)
      // =======================================================

      // Usamos el helper toLocalISOString para enviar la fecha en formato local
      // (sin la 'Z' de UTC) al backend, evitando el desajuste de 1 hora.
      const startLocalStr = toLocalISOString(start);
      const endLocalStr = toLocalISOString(end);

      // Llama al formulario Angular
      const clientData = await getClientDataFromForm(
        startLocalStr,
        endLocalStr,
        duracionMinutos,
        idServicio
      );

      // Mostramos mensaje de error si no llegan datos
      if (!clientData) {
        Swal.fire({
          title: "Reserva Cancelada",
          text: "Debes completar los datos para reservar.",
          icon: "info",
          timer: 2000,
          showConfirmButton: false,
        });
        return info.view.calendar.unselect();
      }

      // Enviar pre-reserva
      await sendPreReservaRequest(
        idServicio,
        startLocalStr,
        endLocalStr,
        clientData
      );

      info.view.calendar.unselect();
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

// ===========================================================
// 4. CARGA DE CONFIGURACIÓN DESDE EL BACKEND
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
// 5. INICIALIZACIÓN AL CARGAR EL DOM
// ===========================================================

document.addEventListener("DOMContentLoaded", loadCalendarConfiguration);
