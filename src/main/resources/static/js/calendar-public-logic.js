/**
 * Función principal que inicializa el calendario con la configuración obtenida del backend.
 * @param {object} config - Objeto de configuración (ServicioConfigDTO) obtenido vía API.
 */
function initCalendar(config) {
  // Extraemos los datos del objeto de configuración para usar en FullCalendar
  const idServicio = config.idServicio;

  const duracionMinutos = config.duracionMinutos;
  const horaApertura = config.horaApertura;
  const horaCierre = config.horaCierre;

  var calendarEl = document.getElementById("calendar");

  if (!calendarEl) {
    console.error("Contenedor del calendario #calendar no encontrado.");
    return;
  }

  // Crucial, Formato de duración: HORA:MINUTO:SEGUNDO

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

  var calendar = new FullCalendar.Calendar(calendarEl, {
    themeSystem: "bootstrap5",
    locale: "es",
    // 1. Configuración de Horario y Duración basada en la respuesta de la API
    slotMinTime: horaApertura, // Restringe el calendario a las horas de trabajo
    slotMaxTime: horaCierre, // Restringe el calendario a las horas de cierre

    slotDuration: `00:${String(15).padStart(2, "0")}:00`, // Define el tamaño de los bloques de tiempo visibles
    snapDuration: slotDurationString, //  Asegura que la selección del usuario (al hacer clic o arrastrar) se "ajuste" exactamente a la duración del servicio

    // Configuraciones de UX
    slotLabelFormat: { hour: "2-digit", minute: "2-digit", meridiem: false }, // Formato 24h
    // 2. Configuración general de la vista
    initialView: "timeGridWeek",
    locale: "es",
    headerToolbar: {
      left: "prev,next today",
      center: "title",
      end: "dayGridMonth,timeGridWeek,timeGridDay",
    },
    editable: false,
    selectable: true,
    selectMirror: true,
    expandRows: true,

    // Prevenir solapamiento. Evita que el usuario seleccione tiempo donde ya hay un evento.
    selectOverlap: false,
    scrollTime: horaApertura, // Hace que el calendario se inicie siempre desplazado hasta la hora de apertura (horaApertura)

    // Restringir la selección a bloques exactos (TRUE PARA NO BLOQUEAR EL RATÓN)
    selectAllow: function (selectInfo) {
      // Permitimos cualquier inicio de selección para evitar el icono de bloqueo.
      // snapDuration hará el ajuste de la duración.
      return true;
    },

    // 3. Conexión para obtener los eventos (Reservas Ocupadas de un Negocio)
    // FullCalendar hará un GET a este endpoint con el ID del servicio
    events: "/public/api/calendario/eventos/" + idServicio,

    // 4. Manejo de la selección de un hueco libre (Validación Final de Duración e Inicio del Proceso de Reserva)
    select: function (info) {
      const diffMs = info.end - info.start;
      const diffMin = diffMs / 1000 / 60;
      const roundedDiffMin = Math.round(diffMin);

      // VALIDACIÓN CRUCIAL: Verificamos que FullCalendar haya ajustado la duración
      if (roundedDiffMin !== duracionMinutos) {
        // Si la duración no es la requerida (esto no debería pasar con snapDuration,
        // pero es el fallback de seguridad)
        Swal.fire({
          title: "Error de Duración",
          text: `El servicio requiere ${duracionMinutos} minutos. La selección fue de ${roundedDiffMin} minutos. Inténtalo de nuevo.`,
          icon: "warning",
          // Configuración clave para que se muestre como notificación flotante (Toast)
          toast: true,
          position: "top",
          timer: 4000, // 4 segundos de duración
          timerProgressBar: true,
          showConfirmButton: false,
          showCloseButton: true,
        });
        calendar.unselect();
        return;
      }

      // Si la duración es correcta, procedemos a la reserva
      sendPreReservaRequest(idServicio, info.startStr, info.endStr);

      calendar.unselect();
    },

    // 5. Manejo de clics en eventos ocupados
    eventClick: function (info) {
      Swal.fire({
        title: "Error!",
        text: "Este turno ya está ocupado: " + info.event.title,
        icon: "error",
        confirmButtonText: "Entendido!",
      });
    },
  });

  calendar.render();
}

/**
 * Función que maneja la carga de la configuración del servicio vía API Fetch.
 * Esta es la primera función que se ejecuta al cargar la página.
 */
async function loadCalendarConfiguration() {
  // 1. Extraer el ID del Servicio de la URL actual
  const pathSegments = window.location.pathname.split("/");

  // Obtenemos el último elemento del array
  let idServicioParaAPI = pathSegments[pathSegments.length - 1];

  // Si el último elemento es una cadena vacía (ej. si la URL termina en '/'),
  // tomamos el penúltimo. Esto es robusto para URLs como /public/calendario/2/
  if (idServicioParaAPI === "") {
    idServicioParaAPI = pathSegments[pathSegments.length - 2];
  }

  // 2. Verificamos que si es un número válido
  if (!idServicioParaAPI || isNaN(idServicioParaAPI)) {
    console.error(
      "Fallo al obtener el ID de Servicio de la URL. Valor obtenido:",
      idServicioParaAPI
    );
    document.getElementById("calendar").innerHTML =
      '<div class="alert alert-danger">No se pudo identificar el servicio a reservar.</div>';
    return;
  }

  try {
    // Hacemos la petición al nuevo endpoint REST para obtener el objeto completo
    const response = await fetch(
      "/public/api/calendario/config/" + idServicioParaAPI
    );

    if (!response.ok) {
      // Lanza error si el backend devuelve 404, 500, etc.
      throw new Error(
        `Error ${response.status}: No se pudo obtener la configuración del servicio.`
      );
    }

    const config = await response.json();

    // Una vez que tenemos el objeto JSON seguro, inicializamos el calendario
    initCalendar(config);
  } catch (error) {
    console.error("Error al cargar la configuración del calendario:", error);
    // Si falla, el cliente no ve el calendario, solo un mensaje de error.
    document.getElementById("calendar").innerHTML =
      '<div class="alert alert-danger">No se pudo cargar la disponibilidad del servicio.</div>';
  }
}

/**
 * Muestra un modal de formulario usando SweetAlert2, recopila datos y los devuelve.
 * @param {string} startStr - Fecha y hora de inicio de la reserva.
 * @param {string} endStr - Fecha y hora de fin de la reserva.
 * @returns {Promise<object|null>} Promesa que resuelve a los datos del cliente o null si cancela.
 */
async function getClientDataFromForm(startStr, endStr) {
  // Formatear las fechas para mostrarlas en el título/texto del modal (Mejor UX)
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

  const { value: formValues } = await Swal.fire({
    title: "Confirma tu Reserva",
    html: `
            <p class="mb-3">Has seleccionado un turno:<br>
            de ${formattedStart} a ${formattedEnd}.</p>
            <hr>
            <input id="swal-input1" class="swal2-input" placeholder="Nombre Completo" required>
            <input id="swal-input2" class="swal2-input" placeholder="Correo Electrónico" type="email" required>
            <input id="swal-input3" class="swal2-input" placeholder="Teléfono (Opcional)" type="tel">
        `,
    focusConfirm: false,
    showCancelButton: true,
    confirmButtonText: "Confirmar Cita",
    cancelButtonText: "Cancelar",
    preConfirm: () => {
      const nombre = document.getElementById("swal-input1").value;
      const email = document.getElementById("swal-input2").value;

      // Validación básica para campos requeridos
      if (!nombre || !email) {
        Swal.showValidationMessage("Por favor, rellena tu nombre y email.");
        return false;
      }

      // Retornamos los valores del formulario
      return {
        nombreCliente: nombre,
        correoElec: email,
        telf: document.getElementById("swal-input3").value,
      };
    },
  });

  // Si el usuario confirma y los datos son válidos, devolvemos los valores
  if (formValues) {
    return formValues;
  }

  // Si cancela o cierra el modal, devolvemos null
  return null;
}

/**
 * Función que ensambla el DTO y envía la solicitud POST al backend.
 * @param {string} idServicio El ID del servicio.
 * @param {string} startStr Fecha y hora de inicio del slot.
 * @param {string} endStr Fecha y hora de fin del slot.
 */
async function sendPreReservaRequest(idServicio, startStr, endStr) {
  // Llama a la función asíncrona y espera el resultado.
  const clientData = await getClientDataFromForm(startStr, endStr);

  if (!clientData) {
    Swal.fire({
      title: "Reserva Cancelada",
      text: "Debes completar los datos para reservar.",
      icon: "info",
      timer: 2000,
      showConfirmButton: false,
    });
    return;
  }

  // Ensamblamos el ReservaRequestDTO
  const reservaRequestDTO = {
    idServicio: idServicio,
    fechaInicio: startStr,
    fechaFin: endStr,
    nombreCliente: clientData.nombreCliente,
    correoElec: clientData.correoElec,
    telf: clientData.telf,
  };

  try {
    const response = await fetch("/public/reservas/crear", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(reservaRequestDTO),
    });

    const responseText = await response.text();

    if (response.status === 202) {
      Swal.fire({
        title: "¡Todo en orden!",
        text: "¡Pre-Reserva exitosa! Revisa tu correo para confirmar la cita.",
        icon: "success",
        confirmButtonText: "¡Entendido!",
      });
    } else {
      // Maneja errores de validación (400, 409 Conflict si ya está ocupado)
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

// Aseguramos que el proceso de carga y inicialización comience cuando el DOM esté listo
document.addEventListener("DOMContentLoaded", loadCalendarConfiguration);
