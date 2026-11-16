/* =========================================================
   AUTOGEST · panel.js
   Comportamiento común de las vistas del panel (con sidebar):
   1. Íconos Lucide cada vez que HTMX inserta contenido.
   2. Abrir y cerrar el modal (<dialog>).
   3. Recepción: Enter busca la placa, "Continuar" muestra la orden.
   4. Mapas Leaflet (marcar el punto en el modal y verlo en el detalle).
   5. Token CSRF en cada petición HTMX.
   6. Modal de confirmación propio para hx-confirm (eliminar, cancelar, entregar…).
   ========================================================= */

const DEFAULT_CENTER = [5.5353, -73.3678]; // Tunja
const TILE_URL = 'https://tile.openstreetmap.org/{z}/{x}/{y}.png';
const TILE_ATTRIBUTION = '&copy; OpenStreetMap';

/* ---------- 1. Íconos ---------- */
function drawIcons() {
    if (window.lucide) {
        lucide.createIcons();
    }
}

/* ---------- 2. Modal ---------- */
function openModals(root) {
    const dialogs = root.matches?.('dialog.modal-overlay')
        ? [root]
        : root.querySelectorAll?.('dialog.modal-overlay') ?? [];

    dialogs.forEach(dialog => {
        if (dialog.open) {
            return;
        }
        // show() y no showModal(): así los toasts (z-index 1100) quedan por encima del modal (z-index 1000)
        dialog.show();
        dialog.querySelector('input:not([type="hidden"]), select, textarea')?.focus();
        // Al cerrarse (Escape, ✕, Cancelar o clic afuera) se elimina del DOM
        dialog.addEventListener('close', () => dialog.remove());
        initPickerMap(dialog);
    });
}

function closeModal(element) {
    element.closest('dialog')?.close();
}

// Escape cierra el modal (con show() el navegador no lo hace solo)
document.addEventListener('keydown', event => {
    if (event.key === 'Escape') {
        document.querySelector('dialog.modal-overlay[open]')?.close();
    }
});

/* ---------- 3. Recepción del vehículo ---------- */
document.addEventListener('keydown', event => {
    if (event.key !== 'Enter' || !event.target.closest('#vehicle-section')) {
        return;
    }
    // Enter dentro de la sección del vehículo no envía la orden
    event.preventDefault();
    if (event.target.id === 'plate') {
        document.getElementById('btn-lookup')?.click();
    }
});

// Muestra "Guardar cambios" solo cuando se modifica el propietario o el celular
document.addEventListener('input', event => {
    if (event.target.matches('#vehicle-section [data-track-change]')) {
        const button = document.getElementById('btn-save-vehicle');
        if (button) {
            button.hidden = false;
        }
    }
});

// Color de placa: al elegir el tipo, su ejemplo pasa al placeholder y a la vista previa
// (si todavía no se ha escrito la placa). Escucha en document porque HTMX carga los modales después.
document.addEventListener('change', event => {
    if (event.target.id !== 'plateColor') {
        return;
    }

    const example = event.target.selectedOptions[0]?.dataset.example || 'ABC-123';
    const input = document.getElementById('plate');
    const preview = document.getElementById('plate-preview');

    if (input) {
        input.placeholder = example;
    }
    if (preview && input && input.value === '') {
        preview.textContent = example;
    }
});

// Buscador de modelo (el fragmento de /vehicles llama a esta función al elegir un resultado):
// guarda el id, muestra "Marca Modelo" y cierra la lista
function selectCarModel(button) {
    const box = button.closest('.autocomplete');
    box.querySelector('[name="carModelId"]').value = button.dataset.id;
    box.querySelector('[name="text"]').value = button.dataset.label;
    box.querySelector('.autocomplete-results').innerHTML = '';
}

// Cierra la lista de modelos si se hace clic fuera del buscador
document.addEventListener('click', event => {
    const results = document.getElementById('car-model-results');
    if (results && !event.target.closest('.autocomplete')) {
        results.innerHTML = '';
    }
});

function showServiceSection() {
    const section = document.getElementById('service-section');
    if (!section) {
        return;
    }
    section.hidden = false;
    document.getElementById('btn-continue')?.setAttribute('hidden', '');
    document.getElementById('reason')?.focus();
}

/* ---------- 4. Mapas ---------- */
function createMap(element, center, zoom) {
    const map = L.map(element).setView(center, zoom);
    L.tileLayer(TILE_URL, { maxZoom: 19, attribution: TILE_ATTRIBUTION }).addTo(map);
    return map;
}

// Mapa del modal: se crea la primera vez que se elige "A domicilio"
function initPickerMap(dialog) {
    const select = dialog.querySelector('#serviceType');
    if (!select || !window.L) {
        return;
    }

    let map = null;
    let marker = null;

    const latInput = dialog.querySelector('#lat');
    const lngInput = dialog.querySelector('#lng');
    const coordsText = dialog.querySelector('#coords-text');

    function setPoint(lat, lng) {
        latInput.value = lat.toFixed(6);
        lngInput.value = lng.toFixed(6);
        coordsText.textContent = `Lat ${latInput.value} · Lng ${lngInput.value}`;
        if (marker) {
            marker.setLatLng([lat, lng]);
        } else {
            marker = L.marker([lat, lng]).addTo(map);
        }
    }

    function ensureMap() {
        if (select.value !== 'SERVICE_EXTERNAL') {
            return;
        }
        if (!map) {
            // Zoom 13: Tunja completa a la vista
            map = createMap(dialog.querySelector('#modal-map'), DEFAULT_CENTER, 13);
            map.on('click', event => setPoint(event.latlng.lat, event.latlng.lng));

            // Edición de una orden que ya tenía ubicación
            if (latInput.value && lngInput.value) {
                const lat = parseFloat(latInput.value);
                const lng = parseFloat(lngInput.value);
                map.setView([lat, lng], 15);
                setPoint(lat, lng);
            }
        }
        // El contenedor estaba oculto: Leaflet necesita recalcular su tamaño
        setTimeout(() => map.invalidateSize(), 50);
    }

    select.addEventListener('change', ensureMap);
    // También cuando el CSS muestra la sección de la orden
    dialog.addEventListener('click', event => {
        if (event.target.id === 'btn-continue') {
            setTimeout(ensureMap, 0);
        }
    });
    ensureMap();
}

// Mapa del detalle: botón "Ver ubicación en el mapa"
function showDetailMap(button) {
    const container = document.getElementById('detail-map');
    if (!container || !window.L || container.dataset.ready) {
        return;
    }
    const point = [parseFloat(button.dataset.lat), parseFloat(button.dataset.lng)];
    container.dataset.ready = 'true';
    const map = createMap(container, point, 15);
    L.marker(point).addTo(map);
    setTimeout(() => map.invalidateSize(), 50);
    button.hidden = true;
}

/* ---------- Fila seleccionada en la tabla ---------- */
document.addEventListener('click', event => {
    // Órdenes (#orders-body) y repuestos (#spare-part-table): las filas con data-id abren un detalle.
    // Los botones de la fila (.row-actions) no cambian la selección.
    const row = event.target.closest('tbody tr[data-id]');
    if (!row || event.target.closest('.row-actions')) {
        return;
    }
    row.parentElement.querySelectorAll('tr.row-selected')
        .forEach(selected => selected.classList.remove('row-selected'));
    row.classList.add('row-selected');
});

/* ---------- 5. CSRF ----------
   fragments/head.html deja el token en <meta name="_csrf"> y el nombre del header en
   <meta name="_csrf_header">. Así no hay que repetir hx-headers en cada <body>. */
document.addEventListener('htmx:configRequest', event => {
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;
    if (token && header) {
        event.detail.headers[header] = token;
    }
});

/* ---------- 6. Modal de confirmación ----------
   Reemplaza la ventana confirm() del navegador en todos los botones con hx-confirm,
   sin cambiar las plantillas: el texto de hx-confirm es la pregunta del modal.
   Usa los mismos estilos del modal de base.css (.modal-overlay, .modal-card).
   · Botones que borran (hx-delete o clase *danger*): tono rojo, ícono de alerta y "Eliminar"/"Sí, cancelar".
   · Los demás (entregar, terminar reparación): tono morado y "Sí, continuar".
   Se cierra con Cancelar, ✕, clic afuera o Escape; solo "confirmar" envía la petición. */
function isDangerAction(element) {
    return element.hasAttribute('hx-delete')
        || element.hasAttribute('data-hx-delete')
        || /danger/.test(element.className);
}

function askConfirmation(question, element) {
    return new Promise(resolve => {
        const danger = isDangerAction(element);
        const deletes = element.hasAttribute('hx-delete');
        const confirmLabel = danger ? (deletes ? 'Eliminar' : 'Sí, cancelar') : 'Sí, continuar';

        const dialog = document.createElement('dialog');
        dialog.className = 'modal-overlay confirm-modal' + (danger ? ' is-danger' : '');
        dialog.setAttribute('aria-labelledby', 'confirm-title');
        dialog.setAttribute('aria-describedby', 'confirm-text');
        dialog.innerHTML = `
            <button type="button" class="modal-dismiss" aria-label="Cerrar" data-answer="no"></button>
            <div class="modal-card confirm-card">
                <span class="confirm-icon"><i data-lucide="${danger ? 'triangle-alert' : 'circle-help'}"></i></span>
                <h2 id="confirm-title">${danger ? 'Confirma esta acción' : '¿Deseas continuar?'}</h2>
                <p id="confirm-text"></p>
                <div class="confirm-actions">
                    <button type="button" class="btn btn-outline" data-answer="no">Cancelar</button>
                    <button type="button" class="btn ${danger ? 'btn-danger' : 'btn-primary'}" data-answer="yes"></button>
                </div>
            </div>`;
        // textContent: la pregunta viene de la plantilla (puede traer nombres de usuario)
        dialog.querySelector('#confirm-text').textContent = question;
        dialog.querySelector('[data-answer="yes"]').textContent = confirmLabel;

        let accepted = false;
        dialog.addEventListener('click', event => {
            const answer = event.target.closest('[data-answer]')?.dataset.answer;
            if (answer) {
                accepted = answer === 'yes';
                dialog.close();
            }
        });
        dialog.addEventListener('close', () => {
            dialog.remove();
            element.focus?.();
            resolve(accepted);
        });

        document.body.append(dialog);
        drawIcons();
        dialog.show();                                   // show(): los toasts quedan por encima
        dialog.querySelector('[data-answer="no"].btn').focus();   // Cancelar enfocado: Enter no borra por accidente
    });
}

document.addEventListener('htmx:confirm', event => {
    const question = event.detail.question;
    if (!question) {
        return;                                          // sin hx-confirm: la petición sigue normal
    }
    event.preventDefault();
    askConfirmation(question, event.detail.elt).then(accepted => {
        if (accepted) {
            event.detail.issueRequest(true);             // true = ya confirmado, no vuelve a preguntar
        }
    });
});

/* ---------- Saludo según la hora (fragments/header :: welcome) ----------
   Spring pone data-daypart con la hora del servidor; aquí se ajusta a la hora
   del navegador: 5-11 días, 12-18 tardes, 19-4 noches. */
function updateDaypart() {
    const hour = new Date().getHours();
    const daypart = hour >= 5 && hour < 12 ? 'morning' : hour < 19 && hour >= 12 ? 'afternoon' : 'night';
    document.querySelectorAll('.welcome[data-daypart]')
        .forEach(welcome => { welcome.dataset.daypart = daypart; });
}
setInterval(updateDaypart, 60000);

/* ---------- Vista de referencia de repuestos ---------- */
// La ✕ del panel lo cierra y la tabla vuelve a ocupar todo el ancho
function closePartDetail() {
    document.getElementById('part-detail')?.classList.remove('is-open');
    document.querySelectorAll('#spare-part-table tr.row-selected')
        .forEach(row => row.classList.remove('row-selected'));
}

/* ---------- Enganche con HTMX ---------- */
document.addEventListener('DOMContentLoaded', () => {
    drawIcons();
    updateDaypart();
});

document.addEventListener('htmx:load', event => {
    const elt = event.detail.elt;
    drawIcons();
    updateDaypart();
    openModals(elt);

    // Orden nueva: si se volvió a la búsqueda ("Cambiar placa"), se oculta otra vez la sección de la orden
    if (elt.id === 'vehicle-section' && !elt.querySelector('[name="vehicleId"]')
        && document.querySelector('#form-new-order[hx-post]')) {
        document.getElementById('service-section').hidden = true;
    }
});

/* ---------- Contraseña cambiada ----------
   AccountController envía la cabecera HX-Trigger {"passwordChanged": "<url>"}.
   Se bloquea el botón (evita un segundo envío) y tras 1,5 s se va a la vista principal del rol. */
document.addEventListener('passwordChanged', event => {
    // setTimeout 0: la respuesta trae un formulario nuevo; se bloquea el botón ya reemplazado
    setTimeout(() => {
        const button = document.querySelector('#password-form [type="submit"]');
        if (button) button.disabled = true;
    }, 0);
    setTimeout(() => { window.location.href = event.detail.value; }, 1500);
});
