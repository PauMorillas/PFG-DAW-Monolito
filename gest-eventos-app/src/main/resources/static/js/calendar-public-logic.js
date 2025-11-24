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

  const slotDurationString =
    duracionMinutos >= 60 && duracionMinutos % 60 === 0
      ? `${String(duracionMinutos / 60).padStart(2, "0")}:00:00`
      : `00:${String(duracionMinutos).padStart(2, "0")}:00`;
  return new FullCalendar.Calendar(calendarEl, {
    themeSystem: "bootstrap5",
    locale: "es",
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
    snapDuration: slotDurationString,
    slotLabelFormat: { hour: "2-digit", minute: "2-digit", meridiem: false },
    businessHours: {
      daysOfWeek: diasAperturaArray,
      startTime: horaApertura,
      endTime: horaCierre,
    },
    scrollTime: horaApertura,
    selectOverlap: false,
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
    select: async (info) => {
      const diffMin = Math.round((info.end - info.start) / 1000 / 60);
      if (diffMin !== duracionMinutos) {
        Swal.fire({
          title: "Error de Duración",
          text: `El servicio requiere ${duracionMinutos} minutos. Seleccionaste ${diffMin} min.`,
          icon: "warning",
          toast: true,
          position: "top",
          timer: 4000,
          timerProgressBar: true,
          showConfirmButton: false,
          showCloseButton: true,
        });
        return info.view.calendar.unselect();
      }

      const clientData = await getClientDataFromForm(
        info.startStr,
        info.endStr,
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
        return info.view.calendar.unselect();
      }

      await sendPreReservaRequest(
        idServicio,
        info.startStr,
        info.endStr,
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
