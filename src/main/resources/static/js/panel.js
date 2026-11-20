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
    const section = event.target.closest('#vehicle-section');
    if (event.key !== 'Enter' || !section) {
        return;
    }
    // Un botón con el foco (ej. "Continuar") se activa con Enter como siempre
    if (event.target.matches('button')) {
        return;
    }
    // Enter dentro de la sección del vehículo no envía la orden
    event.preventDefault();

    // Buscador de modelo: Enter no hace nada (se elige un resultado de la lista)
    if (event.target.id === 'carModelSearch') {
        return;
    }
    if (event.target.id === 'plate' || event.target.id === 'plateColor') {
        document.getElementById('btn-lookup')?.click();
        return;
    }
    // Propietario / celular: si se editaron, primero "Guardar cambios";
    // si no, la acción principal de la sección ("Continuar" o "Registrar vehículo")
    const save = document.getElementById('btn-save-vehicle');
    if (save && !save.hidden) {
        save.click();
    } else {
        section.querySelector('.section-actions .btn-primary:not([hidden])')?.click();
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

// options (opcional): { title, confirmLabel, danger } para casos que no vienen de hx-confirm
function askConfirmation(question, element, options = {}) {
    return new Promise(resolve => {
        const danger = options.danger ?? isDangerAction(element);
        const deletes = element.hasAttribute('hx-delete');
        const confirmLabel = options.confirmLabel
            ?? (danger ? (deletes ? 'Eliminar' : 'Sí, cancelar') : 'Sí, continuar');

        const dialog = document.createElement('dialog');
        dialog.className = 'modal-overlay confirm-modal' + (danger ? ' is-danger' : '');
        dialog.setAttribute('aria-labelledby', 'confirm-title');
        dialog.setAttribute('aria-describedby', 'confirm-text');
        dialog.innerHTML = `
            <button type="button" class="modal-dismiss" aria-label="Cerrar" data-answer="no"></button>
            <div class="modal-card confirm-card">
                <span class="confirm-icon"><i data-lucide="${danger ? 'triangle-alert' : 'circle-help'}"></i></span>
                <h2 id="confirm-title"></h2>
                <p id="confirm-text"></p>
                <div class="confirm-actions">
                    <button type="button" class="btn btn-outline" data-answer="no">Cancelar</button>
                    <button type="button" class="btn ${danger ? 'btn-danger' : 'btn-primary'}" data-answer="yes"></button>
                </div>
            </div>`;
        // textContent: la pregunta viene de la plantilla (puede traer nombres de usuario)
        dialog.querySelector('#confirm-title').textContent =
            options.title ?? (danger ? 'Confirma esta acción' : '¿Deseas continuar?');
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

/* ---------- 7. Tablas editables (repuestos y modelos: tabla con clase .editable-table) ----------
   El servidor hace el trabajo pesado: devuelve la fila en lectura ("row") o en edición ("row-edit").
   Aquí solo van los atajos y las protecciones:
   · Doble clic en una celda   -> abre la fila en edición con el cursor en ESA celda (data-field).
   · Enter en un campo         -> igual que ✓ (guardar).
   · Esc en un campo / ✕       -> cancela; si hubo cambios, pregunta antes de descartarlos.
   · Abrir otra fila           -> una a la vez: la que estaba en edición se cierra primero
                                  (también preguntando si tenía cambios).
   · Imagen (repuestos)        -> vista previa de la imagen elegida y límite de 2 MB. */
let focusFieldAfterEdit = null;     // campo que recibe el cursor cuando llega la fila en edición

// ¿Algún campo de la fila es distinto de lo que trajo el servidor?
function rowHasChanges(row) {
    return [...row.querySelectorAll('input, select, textarea')]
        .some(field => field.value !== field.defaultValue);
}

function confirmDiscard(row) {
    return askConfirmation('Los cambios de esta fila no se han guardado.', row, {
        title: '¿Descartar los cambios?',
        confirmLabel: 'Descartar',
        danger: true,
    });
}

// Cierra la fila que esté en edición. Devuelve false si el usuario quiere conservar sus cambios.
async function closeEditingRow(except) {
    const editing = document.querySelector('.editable-table tr.is-editing');
    if (!editing || editing === except) {
        return true;
    }
    if (rowHasChanges(editing) && !(await confirmDiscard(editing))) {
        editing.querySelector('input')?.focus();
        return false;
    }
    const cancel = editing.querySelector('[data-row-cancel]');
    cancel.dataset.confirmed = 'true';
    cancel.click();
    return true;
}

// Doble clic en una celda editable -> igual que el lápiz, recordando la celda
document.addEventListener('dblclick', event => {
    const cell = event.target.closest('.editable-table tbody tr[data-id]:not(.is-editing) td');
    if (!cell || event.target.closest('.row-actions')) {
        return;
    }
    focusFieldAfterEdit = cell.dataset.field ?? null;
    window.getSelection()?.removeAllRanges();          // el doble clic selecciona texto: se limpia
    cell.closest('tr').dataset.skipDetail = 'true';     // los clics del doble clic no abren el detalle
    cell.closest('tr').querySelector('[data-row-edit]')?.click();
});

// Repuestos: el clic en la fila abre el detalle con delay:300ms; si fue doble clic, se cancela
document.addEventListener('htmx:beforeRequest', event => {
    if (event.detail.elt.dataset?.skipDetail) {
        event.preventDefault();
    }
});

// Clic en el lápiz o en ✕ (fase de captura: llega antes que HTMX y puede detenerlo)
document.addEventListener('click', async event => {
    const edit = event.target.closest('.editable-table [data-row-edit]');
    if (edit) {
        if (edit.dataset.ready) {                       // ya se cerró la fila anterior
            delete edit.dataset.ready;
            return;
        }
        const editing = document.querySelector('.editable-table tr.is-editing');
        if (editing) {
            event.preventDefault();
            event.stopPropagation();
            if (await closeEditingRow()) {
                edit.dataset.ready = 'true';
                edit.click();
            }
        }
        return;
    }

    const cancel = event.target.closest('.editable-table [data-row-cancel]');
    if (cancel && !cancel.dataset.confirmed && rowHasChanges(cancel.closest('tr'))) {
        event.preventDefault();
        event.stopPropagation();
        if (await confirmDiscard(cancel.closest('tr'))) {
            cancel.dataset.confirmed = 'true';
            cancel.click();
        }
    }
}, true);

// Enter guarda, Esc cancela (solo dentro de una fila en edición)
document.addEventListener('keydown', event => {
    const row = event.target.closest('.editable-table tr.is-editing');
    if (!row) {
        return;
    }
    if (event.key === 'Enter') {
        event.preventDefault();
        row.querySelector('[data-row-save]')?.click();
    } else if (event.key === 'Escape') {
        event.preventDefault();
        event.stopPropagation();                       // que no cierre otra cosa (modales)
        row.querySelector('[data-row-cancel]')?.click();
    }
}, true);

// Imagen en la fila (spare-parts): muestra la elegida antes de guardar y revisa el tamaño.
// 2 MB = spring.servlet.multipart.max-file-size de application.properties.
const MAX_IMAGE_BYTES = 2 * 1024 * 1024;

document.addEventListener('change', event => {
    const input = event.target.closest('input[type="file"][data-image-preview]');
    if (!input) {
        return;
    }
    const file = input.files?.[0];
    const box = input.closest('.cell-image');
    if (!file) {
        return;
    }
    if (file.size > MAX_IMAGE_BYTES) {
        input.value = '';
        input.setCustomValidity('La imagen supera 2 MB. Elige una más liviana.');
        input.reportValidity();
        input.setCustomValidity('');
        return;
    }
    // Reemplaza la miniatura (o el ícono vacío) por la imagen elegida
    box.querySelectorAll('img.thumb, .thumb-empty').forEach(old => old.remove());
    const preview = document.createElement('img');
    preview.className = 'thumb';
    preview.alt = 'Nueva imagen del repuesto';
    preview.src = URL.createObjectURL(file);
    preview.onload = () => URL.revokeObjectURL(preview.src);
    box.prepend(preview);
    box.classList.add('has-new-image');
});

// Cuando llega la fila en edición: cursor en la celda del doble clic (o en el primer campo)
document.addEventListener('htmx:load', event => {
    const row = event.detail.elt;
    if (!row.matches?.('tr.is-editing')) {
        return;
    }
    const field = (focusFieldAfterEdit && row.querySelector(`[name="${focusFieldAfterEdit}"]`))
        || row.querySelector('input:not([type="file"])');
    focusFieldAfterEdit = null;
    field?.focus();
    if (field?.type !== 'file') {
        field?.select?.();
    }
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

/* ---------- Sidebar en pantallas angostas ----------
   El botón ☰ (fragments/header :: welcome) abre y cierra el sidebar.
   Se cierra con clic fuera, Escape, al elegir una opción o al volver a pantalla ancha. */
function setSidebarOpen(open) {
    document.body.classList.toggle('sidebar-open', open);
    document.querySelectorAll('.sidebar-toggle').forEach(button => {
        button.setAttribute('aria-expanded', String(open));
        button.setAttribute('aria-label', open ? 'Cerrar menú' : 'Abrir menú');
    });
}

document.addEventListener('click', event => {
    if (event.target.closest('.sidebar-toggle')) {
        setSidebarOpen(!document.body.classList.contains('sidebar-open'));
        return;
    }
    if (!document.body.classList.contains('sidebar-open')) {
        return;
    }
    // Clic fuera del sidebar o en un enlace del menú: se cierra
    if (!event.target.closest('.sidebar') || event.target.closest('.sidebar a')) {
        setSidebarOpen(false);
    }
});

document.addEventListener('keydown', event => {
    if (event.key === 'Escape' && document.body.classList.contains('sidebar-open')) {
        setSidebarOpen(false);
    }
});

window.matchMedia('(min-width: 1025px)').addEventListener('change', event => {
    if (event.matches) {
        setSidebarOpen(false);
    }
});

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

    // HTMX reemplazó la sección y el cursor quedó en el <body>: se pone donde sigue el flujo,
    // así Enter funciona de nuevo (placa encontrada -> "Continuar"; búsqueda -> la placa)
    if (elt.id === 'vehicle-section') {
        const next = elt.querySelector('#btn-continue:not([hidden])')
            || elt.querySelector('#plate')
            || elt.querySelector('#carModelSearch');
        next?.focus();
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
