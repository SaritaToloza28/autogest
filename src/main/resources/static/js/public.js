/* =========================================================
   AUTOGEST · public.js  (portal público, sin sesión)
   1. Íconos Lucide al cargar y cada vez que HTMX inserta contenido.
   2. Buscador en forma de placa: mientras se escribe, reconoce el tipo de
      placa colombiana por su formato y pinta la placa de ese color.

   Es solo una ayuda visual: el backend debe validar la placa y el color.
   Los nombres de color son los del enum PlateColor del backend.
   ========================================================= */

/* ---------- 1. Íconos ---------- */
function drawPublicIcons() {
    if (window.lucide) {
        lucide.createIcons();
    }
}
document.addEventListener('DOMContentLoaded', drawPublicIcons);
document.addEventListener('htmx:load', drawPublicIcons);

/* ---------- 2. Tipos de placa ----------
   label: lo que se muestra debajo de la placa.
   BACKEND: estos textos deberían coincidir con PlateColor.label. */
const PLATE_TYPES = {
    COLOR_YELLOW:            { name: 'Amarilla', label: 'Placa amarilla · Particular' },
    COLOR_WHITE:             { name: 'Blanca',   label: 'Placa blanca · Servicio público' },
    COLOR_GREEN:             { name: 'Verde',    label: 'Placa verde · Remolque' },
    COLOR_BLUE:              { name: 'Azul',     label: 'Placa azul · Diplomática' },
    COLOR_RED:               { name: 'Roja',     label: 'Placa roja · Temporal' },
    COLOR_YELLOW_MOTORCYCLE: { name: 'Moto',     label: 'Placa de moto · Amarilla' },
};

/* Reglas en orden: la primera que coincide gana.
   colors[0] es el color sugerido; los demás se ofrecen como botones.
   Deben coincidir con los regex de PlateColor (backend), aquí sin el guion. */
const PLATE_RULES = [
    { pattern: /^[A-Z]{3}\d{2}[A-Z]$/, colors: ['COLOR_YELLOW_MOTORCYCLE'] },          // ABC-12D
    { pattern: /^(CD|CC|OI)\d{4}$/,    colors: ['COLOR_BLUE'] },                       // CD-1234
    { pattern: /^[A-Z]\d{4}$/,         colors: ['COLOR_RED'] },                        // T-1234
    { pattern: /^[RS]\d{5}$/,          colors: ['COLOR_GREEN'] },                      // R-12345
    { pattern: /^[A-Z]{3}\d{3}$/,      colors: ['COLOR_YELLOW', 'COLOR_WHITE'] },      // ABC-123
];

/* "abc123" -> "ABC-123": letras iniciales, guion y el resto */
function formatPlate(raw) {
    const clean = raw.toUpperCase().replace(/[^A-Z0-9]/g, '');
    const prefix = clean.match(/^[A-Z]{1,3}/)?.[0] ?? '';
    const rest = clean.slice(prefix.length);
    return rest ? `${prefix}-${rest}` : prefix;
}

function detectPlate(value) {
    const clean = value.replace('-', '');
    return PLATE_RULES.find(rule => rule.pattern.test(clean)) ?? null;
}

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('plate-search');
    if (!form) {
        return;
    }
    const input = document.getElementById('licensePlate');
    const plate = document.getElementById('plate-input');
    const colorField = document.getElementById('plateColor');
    const typeText = document.getElementById('plate-type');
    const choices = document.getElementById('plate-choices');
    const defaultText = typeText.textContent.trim();

    function setColor(color) {
        plate.dataset.plate = color ?? 'none';
        colorField.value = color ?? '';
        typeText.textContent = color ? PLATE_TYPES[color].label : defaultText;
        choices.querySelectorAll('button')
            .forEach(button => button.setAttribute('aria-pressed', String(button.dataset.color === color)));
    }

    // Botones para escoger otro color cuando el formato admite varios
    function showChoices(colors, selected) {
        choices.innerHTML = '';
        choices.hidden = colors.length < 2;
        if (choices.hidden) {
            return;
        }
        const title = document.createElement('span');
        title.textContent = '¿Es de otro color?';
        choices.append(title);
        colors.forEach(color => {
            const button = document.createElement('button');
            button.type = 'button';
            button.dataset.color = color;
            button.className = `plate-choice plate-${color}`;
            button.textContent = PLATE_TYPES[color].name;
            button.setAttribute('aria-pressed', String(color === selected));
            button.addEventListener('click', () => setColor(color));
            choices.append(button);
        });
    }

    function update(preferredColor) {
        input.value = formatPlate(input.value);
        const rule = detectPlate(input.value);

        if (!rule) {
            setColor(null);
            showChoices([]);
            if (input.value.length >= 6) {
                typeText.textContent = 'Formato no reconocido. Revisa los formatos de abajo.';
            }
            input.setCustomValidity(input.value ? 'Escribe una placa con un formato válido, por ejemplo ABC-123.' : '');
            return;
        }
        input.setCustomValidity('');
        const color = rule.colors.includes(preferredColor) ? preferredColor : rule.colors[0];
        showChoices(rule.colors, color);
        setColor(color);
    }

    input.addEventListener('input', () => update(colorField.value));

    // Placas de ejemplo
    document.querySelectorAll('[data-example]').forEach(button => {
        button.addEventListener('click', () => {
            input.value = button.dataset.example;
            update(button.dataset.color);
            input.focus();
        });
    });
});
