(function () {
  "use strict";

  // TODO: PRODUCCION CAMBIAR A LA URL DE prod
  function buildAngularUrl(preReserva) {
    const base = "http://localhost:4200/registro-cliente";
    const params = new URLSearchParams({
      parentOrigin: window.location.origin,
      preReservaData: encodeURIComponent(JSON.stringify(preReserva)),
    });
    return `${base}?${params.toString()}`;
  }

  // =======================================================
  // V. COMUNICACIÓN ENTRE CALENDARIO ↔ PADRE ↔ ANGULAR
  // =======================================================

  window.addEventListener("message", (event) => {
    const msg = event.data || {};
    if (msg.type !== "openClientForm") return;

    const preReserva = msg.data;
    const angularUrl = buildAngularUrl(preReserva);

    const childWindow = event.source;
    const childOrigin = event.origin;

    const ANGULAR_ORIGIN = new URL(angularUrl).origin;
    // Creamos un iframe dedicado para Angular
    let angularIframe = document.getElementById("embedbook-angular-iframe");

    if (!angularIframe) {
      angularIframe = document.createElement("iframe");
      angularIframe.id = "embedbook-angular-iframe";
      angularIframe.style.position = "fixed";
      angularIframe.style.top = "0";
      angularIframe.style.left = "0";
      angularIframe.style.width = "100%";
      angularIframe.style.height = "100%";
      angularIframe.style.border = "none";
      angularIframe.style.zIndex = 10000;
      document.body.appendChild(angularIframe);
    }

    // Cargar Angular
    angularIframe.src = angularUrl;

    console.log("[PARENT] Cargando Angular en iframe:", angularUrl);

    // Handler temporal para mensajes desde Angular
    function onAngularMessage(ev) {
      if (ev.origin !== ANGULAR_ORIGIN) return;

      const msg = ev.data || {};

      // --- 1) SOLO procesamos nuestros propios tipos ---
      if (msg.type === "clienteData") {
        childWindow.postMessage(
          { type: "clienteData", data: msg.data },
          childOrigin
        );
        cleanup();
        return;
      }

      if (msg.type === "cancel") {
        childWindow.postMessage({ type: "cancel" }, childOrigin);
        cleanup();
        return;
      }

      // --- 2) SI ES CUALQUIER OTRA COSA: NO CERRAMOS EL IFRAME ---
      console.log("[PARENT] Mensaje ignorado desde Angular:", msg);
    }

    function cleanup() {
      window.removeEventListener("message", onAngularMessage);
      const frame = document.getElementById("embedbook-angular-iframe");
      if (frame) frame.remove();
      console.log("[PARENT] Limpieza completa: iframe Angular eliminado");
    }

    // Activar listener temporal
    window.addEventListener("message", onAngularMessage);
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
    // Ocultar el modal al hacer click en 'X'
    closeModalBtn.addEventListener("click", () => {
      // Cerrar modal SÓLO si no hay un iframe Angular abierto
      const angularOpen = !!document.getElementById("embedbook-angular-iframe");
      if (angularOpen) {
        // opcional: pedir confirmación antes de cerrar
        if (!confirm("Hay un formulario abierto. ¿Deseas cerrarlo?")) return;
        // si confirma, forzamos cleanup del iframe angular
        const frame = document.getElementById("embedbook-angular-iframe");
        if (frame) frame.remove();
      }
      modalElement.classList.add("embedbook-hidden");
    });

    // Ocultar el modal al hacer click en el fondo (fuera del contenido)
    modalElement.addEventListener("click", (e) => {
      // Asegurarnos de que el click fue en el backdrop (no en el contenido)
      if (e.target !== modalElement) return;

      // Si hay un formulario Angular abierto, NO cerramos automáticamente.
      if (document.getElementById("embedbook-angular-iframe")) {
        // Evitamos cerrar el modal si Angular está en pantalla: el usuario puede usar la X.
        // (Puedes mostrar confirmación si lo prefieres)
        console.log(
          "[PARENT] Click en backdrop ignorado porque Angular está abierto"
        );
        return;
      }

      modalElement.classList.add("embedbook-hidden");
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
