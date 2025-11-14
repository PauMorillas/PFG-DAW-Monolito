(function () {
  "use strict";

  /* window.addEventListener("message", async (e) => {
    const ANGULAR_ORIGIN = "http://localhost:4200";
    try {
      const origin = e.origin;
      // Validar orígenes permitidos (ajusta según necesites)
      const allowedParentOrigins = [
        window.location.origin,
        ANGULAR_ORIGIN,
      ];

      if (!allowedParentOrigins.includes(origin)) {
        console.warn("[PARENT] mensaje de origen no permitido", origin, e.data);
        return;
      }

      const msg = e.data || {};
      if (msg.type === "openClientForm") {
        // Datos de pre-reserva enviados desde el iframe calendario
        const preReserva = msg.data || {};
        // Construir ANGULAR url con params para que Angular sepa quién es el parent y reciba la preReserva
        const ANGULAR_BASE = ANGULAR_ORIGIN + "registro-cliente";
        const params = new URLSearchParams({
          parentOrigin: window.location.origin,
          preReservaData: encodeURIComponent(JSON.stringify(preReserva)),
        });
        const ANGULAR_URL = `${ANGULAR_BASE}?${params.toString()}`;

        // Cambiar el iframe (sigue siendo el mismo iframe que ya existe)
        const iframe = document.getElementById("embedbook-iframe");
        if (iframe) {
          iframe.src = ANGULAR_URL;
        } else {
          console.error("[PARENT] embedbook-iframe no encontrado");
        }

        // Ahora añadimos un listener temporal para recibir la respuesta del formulario Angular
        function onClientMessage(event) {
          if (
            event.origin !== window.location.origin &&
            event.origin !== "http://localhost:4200"
          ) {
            console.warn(
              "[PARENT] origen no permitido en clienteData",
              event.origin
            );
            return;
          }
          const d = event.data || {};
          if (d.type === "clienteData") {
            window.removeEventListener("message", onClientMessage);
            const cliente = d.data;
            // Enviar la pre-reserva al backend desde el padre
            fetch("/public/reservas/crear", {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify({
                idServicio: preReserva.idServicio,
                fechaInicio: preReserva.startStr,
                fechaFin: preReserva.endStr,
                nombreCliente: cliente.nombreCliente,
                correoElec: cliente.correoElec,
                telf: cliente.telf,
              }),
            })
              .then((resp) =>
                resp.text().then((text) => ({ status: resp.status, text }))
              )
              .then(({ status, text }) => {
                if (status === 202) {
                  alert(
                    "Reserva pendiente! Revisa tu correo para confirmar la cita."
                  );
                } else {
                  alert("Error al crear la pre-reserva: " + text);
                }
                // volver a la vista calendario
                iframe.src = `${window.location.origin}/public/servicios/${preReserva.idServicio}`;
              })
              .catch((err) => {
                console.error("[PARENT] fallo al crear reserva", err);
                alert("Error de conexión al crear la reserva.");
                iframe.src = `${window.location.origin}/public/servicios/${preReserva.idServicio}`;
              });
          } else if (d.type === "cancel") {
            window.removeEventListener("message", onClientMessage);
            // volver a calendario
            const iframe = document.getElementById("embedbook-iframe");
            iframe &&
              (iframe.src = `${window.location.origin}/public/servicios/${preReserva.idServicio}`);
          }
        }

        window.addEventListener("message", onClientMessage);
      }
    } catch (err) {
      console.error("[PARENT] error en message handler", err);
    }
  }); */
window.addEventListener("message", (event) => {
  const msg = event.data || {};
  if (msg.type !== "openClientForm") return;

  // quien pide: ventana child (iframe calendario)
  const childWindow = event.source;
  const childOrigin = event.origin; // recuerda reenviarlo al origen correcto

  const preReserva = msg.data || {};
  const ANGULAR_ORIGIN = "http://localhost:4200";
  const ANGULAR_BASE = `${ANGULAR_ORIGIN}/registro-cliente`;

  // Construimos query params: parentOrigin para que angular pueda postMessage al padre (this window)
  const params = new URLSearchParams({
    parentOrigin: window.location.origin,
    preReservaData: encodeURIComponent(JSON.stringify(preReserva)),
  });

  const angularUrl = `${ANGULAR_BASE}?${params.toString()}`;

  // Cambiar el iframe del padre para cargar Angular (este iframe lo controla el padre)
  const angularIframe = document.getElementById("embedbook-iframe");
  if (!angularIframe) {
    console.error("[PARENT] embedbook-iframe no encontrado");
    // informamos al child que no se pudo abrir
    try { childWindow.postMessage({ type: "cancel" }, childOrigin); } catch(e){/*ignored*/ }
    return;
  }

  // Cargamos Angular
  angularIframe.src = angularUrl;

  // Listener temporal para mensajes desde Angular (ev.origin debe ser ANGULAR_ORIGIN)
  function onAngularMessage(ev) {
    if (ev.origin !== ANGULAR_ORIGIN) return;
    const data = ev.data || {};

    if (data.type === "clienteData") {
      // reenvía al iframe calendario (childWindow)
      try {
        childWindow.postMessage({ type: "clienteData", data: data.data }, childOrigin);
      } catch (err) {
        console.error("[PARENT] fallo re-enviando clienteData al child", err);
      }
      window.removeEventListener("message", onAngularMessage);
      // opcional: volver el iframe al calendario-view if quieres
      // angularIframe.src = `${window.location.origin}/public/servicios/${preReserva.idServicio}`;
    } else if (data.type === "cancel") {
      try { childWindow.postMessage({ type: "cancel" }, childOrigin); } catch(e){}
      window.removeEventListener("message", onAngularMessage);
      // opcional: reset iframe
    }
  }

  window.addEventListener("message", onAngularMessage);

  // Safety: limpiar listener si no hay respuesta en X ms
  const CLEANUP_MS = 120000; // 2 min
  const cleanupTimer = setTimeout(() => {
    window.removeEventListener("message", onAngularMessage);
    try { childWindow.postMessage({ type: "cancel" }, childOrigin); } catch(e){}
  }, CLEANUP_MS);

  // al recibir del angular o reenviar, clearTimeout
  const origOnAngular = onAngularMessage;
  window.addEventListener("message", function onceCleaner(ev) {
    if (ev.origin === ANGULAR_ORIGIN && (ev.data && (ev.data.type === "clienteData" || ev.data.type === "cancel"))) {
      clearTimeout(cleanupTimer);
      window.removeEventListener("message", onceCleaner);
    }
  });
});

  // =======================================================
  // I. CONFIGURACIÓN GLOBAL
  // =======================================================

  const config = {
    // Obtiene el serviceId del atributo data-service-id del script, default a '1' si no existe
    serviceId: document.currentScript.getAttribute("data-service-id") || "1",
    baseUrl: "http://localhost:8081", // TODO: Cambiar a URL de Producción
    modalId: "embedbook-modal",
    buttonId: "embedbook-floating-btn",
    iframeHeightOffset: 25, // Espacio para el botón de cerrar en px
  };

  const iframeUrl = `${config.baseUrl}/public/servicios/${config.serviceId}`;

  let modalElement; // Almacenará la referencia al modal
  let buttonElement; // Almacenará la referencia al botón

  // =======================================================
  // II. FUNCIONES DE CREACIÓN DE ELEMENTOS
  // =======================================================

  /**
   * Crea e inserta el botón flotante en el DOM.
   * @returns {HTMLElement} El elemento botón creado.
   */
  function createFloatingButton() {
    const button = document.createElement("button");
    button.id = config.buttonId;
    button.textContent = "Reservar Cita";
    document.body.appendChild(button);
    return button;
  }

  /**
   * Crea e inserta el modal y su contenido (incluyendo el iFrame).
   * @returns {HTMLElement} El elemento modal principal creado.
   */
  function createModal() {
    // --- Modal principal ---
    const modal = document.createElement("div");
    modal.id = config.modalId;
    modal.classList.add("embedbook-hidden");

    // --- Contenedor de contenido ---
    const modalContent = document.createElement("div");
    modalContent.classList.add("embedbook-modal-content");

    // --- Botón de cerrar ---
    const closeModalBtn = document.createElement("span");
    closeModalBtn.classList.add("embedbook-close-btn");
    closeModalBtn.innerHTML = "&times;";
    modalContent.appendChild(closeModalBtn);

    // --- iFrame ---
    const iframe = document.createElement("iframe");
    iframe.src = iframeUrl;
    iframe.style.width = "100%";
    iframe.style.height = `calc(100% - ${config.iframeHeightOffset}px)`;
    iframe.style.border = "none";
    iframe.id = "embedbook-iframe";
    iframe.allow = "autofocus *; fullscreen *";

    // Ensamblar
    modalContent.appendChild(iframe);
    modal.appendChild(modalContent);
    document.body.appendChild(modal);

    return modal;
  }

  // =======================================================
  // III. FUNCIONES DE LÓGICA Y EVENTOS
  // =======================================================

  /**
   * Asigna los manejadores de eventos al botón y al modal.
   */
  function setupEventListeners() {
    const closeModalBtn = modalElement.querySelector(".embedbook-close-btn");

    // Mostrar el modal al hacer click en el botón
    buttonElement.addEventListener("click", () => {
      modalElement.classList.remove("embedbook-hidden");
    });

    // Ocultar el modal al hacer click en 'X'
    closeModalBtn.addEventListener("click", () => {
      modalElement.classList.add("embedbook-hidden");
    });

    // Ocultar el modal al hacer click en el fondo (fuera del contenido)
    modalElement.addEventListener("click", (e) => {
      if (e.target === modalElement) {
        modalElement.classList.add("embedbook-hidden");
      }
    });
  }

  /**
   * Inyecta los estilos CSS necesarios en la cabecera del documento.
   */
  function injectStyles() {
    const style = document.createElement("style");
    style.textContent = `
        /* Botón flotante */
        #embedbook-floating-btn {
            position: fixed;
            bottom: 20px;
            right: 20px;
            z-index: 1000;
            padding: 15px 25px;
            background-color: #007bff;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
        }

        /* Modal (Fondo oscuro) */
        #${config.modalId} {
            position: fixed;
            z-index: 1001;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            overflow: auto; /* Mantenemos overflow: auto aquí para que el fondo se pueda scrollear si la ventana es muy pequeña */
            background-color: rgba(0, 0, 0, 0.7);
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .embedbook-hidden {
            display: none !important;
        }

        /* Contenido del Modal (Caja Blanca) */
        .embedbook-modal-content {
            background-color: #fefefe;
            /* AJUSTE CLAVE: Reducir el padding derecho y asegurar que el contenido no haga scroll */
            padding: 20px 10px 20px 20px; 
            border: 1px solid #888;
            border-radius: 8px;
            width: 90%; 
            max-width: 800px; 
            height: 90%; 
            max-height: 650px;
            position: relative;
            overflow-y: hidden;
        }

    /* Botón de cerrar: usar caja cuadrada y centrar la X con flexbox
       - display: inline-flex para centrar el contenido horizontal/verticalmente
       - box-sizing: border-box para que padding/border no cambien el ancho/alto declarados
       - line-height: 1 y padding:0 para evitar distorsiones con la tipografía
    */
    .embedbook-close-btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 1.563rem; /* ancho fijo */
      height: 1.563rem; /* alto fijo */
      box-sizing: border-box;
      color: #555;
      font-size: 0.75rem; /* tamaño de la 'x' */
      line-height: 1;
      font-weight: 600;
      cursor: pointer;
      position: absolute;
      /* Ajusta la posición respecto al contenedor*/
      right: 20px;
      top: 8px;
      z-index: 1003;
      background: #fefefe;
      border-radius: 50%;
      padding: 0;
      border: 1px solid rgba(0,0,0,0.08);
      /* Evitar que el icono se mueva en dispositivos con subpixel rounding */
      -webkit-font-smoothing: antialiased;
      -moz-osx-font-smoothing: grayscale;
    }

    .embedbook-close-btn:hover,
    .embedbook-close-btn:focus {
      color: #fff;
      background: transparent;
      text-decoration: none;
    }
    `;
    document.head.appendChild(style);
  }

  // =======================================================
  // IV. FUNCIÓN DE INICIALIZACIÓN PRINCIPAL
  // =======================================================

  /**
   * Inicializa el widget de reserva: crea elementos, inyecta estilos y configura eventos.
   */
  function initializeWidget() {
    // Asegurarse de que el DOM está listo antes de agregar elementos
    if (document.body) {
      buttonElement = createFloatingButton();
      modalElement = createModal();
      injectStyles();
      setupEventListeners();
    } else {
      // Fallback: si el script se carga en el head, esperar a que el body exista
      document.addEventListener("DOMContentLoaded", initializeWidget);
    }
  }

  // Ejecutar el módulo de inicialización
  initializeWidget();
})(); // <--- Esta parte invoca la función anónima inmediatamente.
