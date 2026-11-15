
/* =========================================================
   AUTOGEST · panel.js
   Comportamiento mínimo del dashboard de órdenes:
   1. Íconos Lucide cada vez que HTMX inserta contenido.
   2. Abrir y cerrar el modal (<dialog>).
   3. Recepción: Enter busca la placa, "Continuar" muestra la orden.
   4. Mapas Leaflet (marcar el punto en el modal y verlo en el detalle).
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
    const row = event.target.closest('#orders-body tr[data-id]');
    if (!row) {
        return;
    }
    document.querySelectorAll('#orders-body tr.row-selected')
        .forEach(selected => selected.classList.remove('row-selected'));
    row.classList.add('row-selected');
});

/* ---------- Enganche con HTMX ---------- */
document.addEventListener('DOMContentLoaded', drawIcons);

document.addEventListener('htmx:load', event => {
    const elt = event.detail.elt;
    drawIcons();
    openModals(elt);

    // Orden nueva: si se volvió a la búsqueda ("Cambiar placa"), se oculta otra vez la sección de la orden
    if (elt.id === 'vehicle-section' && !elt.querySelector('[name="vehicleId"]')
        && document.querySelector('#form-new-order[hx-post]')) {
        document.getElementById('service-section').hidden = true;
    }
});
