(function () {
  "use strict";

  // =======================================================
  // 1. CONFIGURACIÓN GLOBAL
  // =======================================================

  const config = {
    serviceId: document.currentScript.getAttribute("data-service-id") || "2",
    baseUrl: "https://embedbookapp.com",
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
    const base =
      "https://paumorillas.github.io/PFG-DAW-ANGULARFRONT/registro-cliente"; // TODO: Cambiar a producción
    const params = new URLSearchParams({
      parentOrigin: window.location.origin,
      preReservaData: encodeURIComponent(JSON.stringify(preReserva)),
    });
    console.log(`${base}?${params.toString()}`);
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
    const modalContent = document.querySelector(".embedbook-modal-content");
    const frameArea = modalContent.querySelector(".embedbook-frame-area");

    let angularIframe = document.getElementById("embedbook-angular-iframe");

    if (!angularIframe) {
      angularIframe = document.createElement("iframe");
      angularIframe.id = "embedbook-angular-iframe";

      // Se posiciona ABSOLUTE dentro del wrapper (frameArea)
      angularIframe.style.position = "absolute";
      angularIframe.style.inset = "0"; // respetará el padding del wrapper
      angularIframe.style.width = "100%";
      angularIframe.style.height = "100%";
      angularIframe.style.border = "none";
      angularIframe.style.zIndex = "10002";
      angularIframe.style.background = "white";
      angularIframe.style.display = "block"; // visible cuando creado

      // Insertamos sobre el iframe principal dentro del wrapper
      // si el iframe principal existe lo dejamos detrás
      frameArea.appendChild(angularIframe);
    } else {
      // Si ya existe, sólo lo mostramos y actualizamos src
      angularIframe.style.display = "block";
    }

    angularIframe.src = angularUrl;

    return angularIframe;
  }

  /**
   * Crea el botón flotante para abrir el modal.
   * @returns {HTMLElement}
   */
  function createFloatingButton() {
    const btn = document.createElement("div");
    btn.id = config.buttonId;
    btn.classList.add("embedbook-tab");

    btn.innerHTML = `
    <div class="eb-tab-inner">
      <span class="eb-label">Pedir Cita</span>
      <span class="eb-arrow">›</span>
    </div>
  `;

    document.body.appendChild(btn);
    return btn;
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

    // === HEADER (NUEVO): contiene la X ===
    const header = document.createElement("div");
    header.classList.add("embedbook-header-area");

    const closeModalBtn = document.createElement("span");
    closeModalBtn.classList.add("embedbook-close-btn");
    closeModalBtn.innerHTML = "&times;";
    header.appendChild(closeModalBtn);

    modalContent.appendChild(header);

    // === WRAPPER DE IFRAMES (SEGUNDA FILA) ===
    const frameArea = document.createElement("div");
    frameArea.classList.add("embedbook-frame-area");
    modalContent.appendChild(frameArea);

    // iframe principal
    const iframe = document.createElement("iframe");
    iframe.src = iframeUrl;
    iframe.id = "embedbook-iframe";
    iframe.allow = "autofocus *; fullscreen *";
    frameArea.appendChild(iframe);

    // === NAVBAR (TERCERA FILA) ===
    const navBar = document.createElement("div");
    navBar.id = "embedbook-iframe-nav";
    navBar.innerHTML = `
      <button id="eb-back-btn">⟵</button>
      <button id="eb-forward-btn">⟶</button>
  `;
    modalContent.appendChild(navBar);

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
/* --- BOTÓN TAB --- */
#${config.buttonId}.embedbook-tab {
  position: fixed;
  top: 90%;
  right: -6px;
  transform: translateY(-50%);
  width: 54px;
  height: 110px;
  background: #007bff;
  border-radius: 12px 0 0 12px;
  cursor: pointer;
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: width .28s cubic-bezier(.22,1,.36,1),
              height .28s cubic-bezier(.22,1,.36,1);
  box-shadow: 0 2px 8px rgba(0,0,0,.25);
  will-change: width, height, transform;
}

/* Contenido interno */
#${config.buttonId} .eb-tab-inner {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8px;
  color: white;
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
  transform: rotate(-90deg);
  transform-origin: center;
  pointer-events: none;
}

/* Flecha */
#${config.buttonId} .eb-arrow {
  display: inline-block;
  font-size: 18px;
  transition: transform .25s ease;
  line-height: 1;
}

/* Estado expandido */
#${config.buttonId}.expanded {
  width: 230px;
  height: 56px;
}
#${config.buttonId}.expanded .eb-tab-inner {
  transform: rotate(0deg);
  pointer-events: auto;
}
#${config.buttonId}.expanded .eb-arrow {
  transform: rotate(90deg);
}

/* --- ANIMACIÓN POKE (CSS) --- */
@keyframes eb-poke {
  0%   { transform: translateY(-50%) translateX(0); }
  40%  { transform: translateY(-50%) translateX(-6px); }
  70%  { transform: translateY(-50%) translateX(-2px); }
  100% { transform: translateY(-50%) translateX(0); }
}
#${config.buttonId}.poking {
  animation: eb-poke .55s ease-out;
}

/* --- MODAL GENERAL --- */
#${config.modalId} { 
  position: fixed;
  z-index: 1001; 
  left: 0; top: 0;
  width: 100%; height: 100%;
  background: rgba(0,0,0,0.7);
  display: flex;
  justify-content: center;
  align-items: center;
}
.embedbook-hidden { display:none!important; }

/* --- CONTENEDOR DEL MODAL (GRID DE 3 FILAS) --- */
.embedbook-modal-content {
  background:#fff;
  width:90%;
  max-width:800px;
  height:90%;
  max-height:650px;
  border-radius:8px;
  position:relative;
  overflow:hidden;

  display: grid;
  grid-template-rows: 
    48px   /* HEADER */
    1fr    /* IFRAMES */
    40px;  /* NAVBAR */

  padding: 0;
}

/* === HEADER DONDE VIVE LA X (y nunca se tapa) === */
.embedbook-header-area {
  position: relative;
  width: 100%;
  height: 48px;
  padding: 8px 20px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  box-sizing: border-box;
  background:white;
  z-index: 20;
}

/* BOTÓN CERRAR */
.embedbook-close-btn {
  width:1.563rem;
  height:1.563rem;
  border-radius:50%;
  border:1px solid rgba(0,0,0,0.08);
  background:#fff;
  cursor:pointer;
  display:flex;
  align-items:center;
  justify-content:center;
  font-size: 20px;
  z-index: 30;
}

/* --- WRAPPER PARA LOS IFRAMES (fila central) --- */
.embedbook-frame-area {
  position: relative;
  padding: 16px; /* 🔥 margen real que SIEMPRE se respeta */
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: transparent;
}

/* Iframe principal */
#embedbook-iframe {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  border: none;
  border-radius: 6px;
  z-index: 1;
  background: white;
}

/* Iframe Angular */
#embedbook-angular-iframe {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  border: none;
  border-radius: 6px;
  z-index: 2;
  background: white;
  display: none;
}

/* --- NAVBAR (fila inferior) --- */
#embedbook-iframe-nav {
  width: 100%;
  height: 40px;
  display: flex;
  justify-content: center;
  gap: 20px;
  align-items: center;
  background: #f8f8f8;
  border-top: 1px solid #ddd;
  box-sizing: border-box;
  padding: 6px;
}

#embedbook-iframe-nav button {
  background: #007bff;
  color: white;
  border: none;
  padding: 4px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
}

  `;
    document.head.appendChild(style);
  }

  function setupFloatingButtonInteractions(btn) {
    let collapseTimeout;
    let pokeInterval;
    let isHovered = false;
    let isAnimatingPoke = false;

    // Expandir
    const expand = () => {
      clearTimeout(collapseTimeout);
      btn.classList.add("expanded");
      isHovered = true;
    };

    // Contraer
    const collapse = () => {
      if (!isHovered) return;
      collapseTimeout = setTimeout(() => {
        btn.classList.remove("expanded");
        isHovered = false;
      }, 200);
    };

    // Hover realista controlado por JS
    btn.addEventListener("mouseenter", expand);
    btn.addEventListener("mouseleave", collapse);
    btn.addEventListener("click", () =>
      modalElement.classList.remove("embedbook-hidden")
    );

    // ANIMACIÓN "POKE" PERIÓDICA
    const startPoke = () => {
      pokeInterval = setInterval(() => {
        if (isHovered || isAnimatingPoke) return;

        isAnimatingPoke = true;

        btn.animate(
          [
            { transform: "translateY(-50%) translateX(0)" },
            { transform: "translateY(-50%) translateX(-6px)" },
            { transform: "translateY(-50%) translateX(0)" },
          ],
          {
            duration: 550,
            easing: "ease-out",
          }
        ).onfinish = () => {
          isAnimatingPoke = false;
        };
      }, 4250); // cada 4s con 25ms
    };
    startPoke();
  }

  // =======================================================
  // 5. EVENTOS Y LOGICA DE INTERACCIÓN
  // =======================================================

  /**
   * Asigna eventos para mostrar/ocultar modal.
   */
  function setupEventListeners() {
    const closeModalBtn = modalElement.querySelector(".embedbook-close-btn");
    const backBtn = document.getElementById("eb-back-btn");
    const forwardBtn = document.getElementById("eb-forward-btn");

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

    // Navegación del iframe
    backBtn.addEventListener("click", () => {
      const iframe = document.getElementById("embedbook-iframe");
      if (iframe?.contentWindow) iframe.contentWindow.history.back();
    });

    forwardBtn.addEventListener("click", () => {
      const iframe = document.getElementById("embedbook-iframe");
      if (iframe?.contentWindow) iframe.contentWindow.history.forward();
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
    setupFloatingButtonInteractions(buttonElement);
    setupEventListeners();
  }

  initializeWidget();
})();
