(function () {
  "use strict";

  // =======================================================
  // 1. CONFIGURACIÓN GLOBAL
  // =======================================================

  const config = {
    serviceId: document.currentScript.getAttribute("data-service-id") || "1",
    baseUrl: "http://localhost:8081", // TODO: Cambiar a URL de Producción
    modalId: "embedbook-modal",
    buttonId: "embedbook-floating-btn",
    iframeHeightOffset: 25, // Espacio para el botón de cerrar en px
  };

  const iframeUrl = `${config.baseUrl}/public/servicios/${config.serviceId}`;

  let modalElement; // Referencia al modal principal
  let buttonElement; // Referencia al botón flotante

  // =======================================================
  // 2. UTILIDADES
  // =======================================================

  /**
   * Construye la URL para abrir Angular, incluyendo datos de pre-reserva y origen del padre.
   * @param {Object} preReserva Datos de pre-reserva
   * @returns {string} URL completa para Angular
   */
  function buildAngularUrl(preReserva) {
    const base = "http://localhost:4200/registro-cliente"; // TODO: Cambiar a producción
    const params = new URLSearchParams({
      parentOrigin: window.location.origin,
      preReservaData: encodeURIComponent(JSON.stringify(preReserva)),
    });
    return `${base}?${params.toString()}`;
  }

  // =======================================================
  // 3. COMUNICACIÓN PADRE ↔ ANGULAR
  // =======================================================

  /**
   * Maneja mensajes recibidos desde otros iframes o ventanas.
   * @param {MessageEvent} event Evento de mensaje
   */
  function handleParentMessage(event) {
    const msg = event.data || {};
    if (msg.type !== "openClientForm") return; // Ignorar mensajes que no sean para abrir el formulario

    const preReserva = msg.data;
    const angularUrl = buildAngularUrl(preReserva);
    const childWindow = event.source;
    const childOrigin = event.origin;
    const ANGULAR_ORIGIN = new URL(angularUrl).origin;

    // Crea el iframe para Angular
    createAngularIframe(angularUrl);

    /**
     * Procesa mensajes enviados desde el iframe Angular.
     * @param {MessageEvent} ev
     */
    function onAngularMessage(ev) {
      if (ev.origin !== ANGULAR_ORIGIN) return; // Ignorar mensajes de otros orígenes

      const msg = ev.data || {};

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

      // Ignorar otros mensajes
    }

    /**
     * Limpieza del listener y eliminación del iframe Angular.
     */
    function cleanup() {
      window.removeEventListener("message", onAngularMessage);
      const frame = document.getElementById("embedbook-angular-iframe");
      if (frame) frame.remove();
    }

    window.addEventListener("message", onAngularMessage);
  }

  window.addEventListener("message", handleParentMessage);

  // =======================================================
  // 4. CREACIÓN DE ELEMENTOS DOM
  // =======================================================

  /**
   * Crea un iframe Angular para mostrar el formulario de cliente.
   * Si ya existe, lo reutiliza.
   * @param {string} angularUrl URL que debe cargar el iframe
   * @returns {HTMLIFrameElement} Referencia al iframe creado o existente
   */
  function createAngularIframe(angularUrl) {
    // Intentamos obtener iframe existente
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

    // Asignamos la URL
    angularIframe.src = angularUrl;

    return angularIframe;
  }

  /**
   * Crea el botón flotante para abrir el modal.
   * @returns {HTMLElement}
   */
  function createFloatingButton() {
    const button = document.createElement("button");
    button.id = config.buttonId;
    button.textContent = "Reservar Cita";
    document.body.appendChild(button);
    return button;
  }

  /**
   * Crea el modal principal con iframe interno.
   * @returns {HTMLElement}
   */
  function createModal() {
    const modal = document.createElement("div");
    modal.id = config.modalId;
    modal.classList.add("embedbook-hidden");

    const modalContent = document.createElement("div");
    modalContent.classList.add("embedbook-modal-content");

    const closeModalBtn = document.createElement("span");
    closeModalBtn.classList.add("embedbook-close-btn");
    closeModalBtn.innerHTML = "&times;";
    modalContent.appendChild(closeModalBtn);

    const iframe = document.createElement("iframe");
    iframe.src = iframeUrl;
    iframe.style.width = "100%";
    iframe.style.height = `calc(100% - ${config.iframeHeightOffset}px)`;
    iframe.style.border = "none";
    iframe.id = "embedbook-iframe";
    iframe.allow = "autofocus *; fullscreen *";

    modalContent.appendChild(iframe);
    modal.appendChild(modalContent);
    document.body.appendChild(modal);

    return modal;
  }

  /**
   * Inyecta estilos necesarios para el modal y botón flotante.
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
        box-shadow: 0 4px 8px rgba(0,0,0,0.2);
      }
      /* Modal fondo oscuro */
      #${config.modalId} {
        position: fixed;
        z-index: 1001;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        overflow: auto;
        background-color: rgba(0,0,0,0.7);
        display: flex;
        justify-content: center;
        align-items: center;
      }
      .embedbook-hidden { display: none !important; }
      .embedbook-modal-content {
        background-color: #fefefe;
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
      .embedbook-close-btn {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 1.563rem;
        height: 1.563rem;
        box-sizing: border-box;
        color: #555;
        font-size: 0.75rem;
        font-weight: 600;
        cursor: pointer;
        position: absolute;
        right: 20px;
        top: 8px;
        z-index: 1003;
        background: #fefefe;
        border-radius: 50%;
        padding: 0;
        border: 1px solid rgba(0,0,0,0.08);
      }
    `;
    document.head.appendChild(style);
  }

  // =======================================================
  // 5. EVENTOS Y LOGICA DE INTERACCIÓN
  // =======================================================

  /**
   * Asigna eventos para mostrar/ocultar modal.
   */
  function setupEventListeners() {
    const closeModalBtn = modalElement.querySelector(".embedbook-close-btn");

    // Mostrar modal al hacer click en el botón
    buttonElement.addEventListener("click", () =>
      modalElement.classList.remove("embedbook-hidden")
    );

    // Cerrar modal al hacer click en "X"
    closeModalBtn.addEventListener("click", () =>
      modalElement.classList.add("embedbook-hidden")
    );

    // Cerrar modal al hacer click en el fondo (backdrop)
    modalElement.addEventListener("click", (e) => {
      if (e.target === modalElement) {
        modalElement.classList.add("embedbook-hidden");
      }
    });
  }

  // =======================================================
  // 6. INICIALIZACIÓN PRINCIPAL
  // =======================================================

  /**
   * Inicializa el widget de reserva: elementos, estilos y eventos.
   */
  function initializeWidget() {
    if (!document.body) {
      document.addEventListener("DOMContentLoaded", initializeWidget);
      return;
    }
    buttonElement = createFloatingButton();
    modalElement = createModal();
    injectStyles();
    setupEventListeners();
  }

  initializeWidget();
})();
