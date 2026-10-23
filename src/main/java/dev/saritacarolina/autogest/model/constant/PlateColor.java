package dev.saritacarolina.autogest.model.constant;

import java.util.regex.Pattern;

public enum PlateColor {

    //Deben iniciar de la misma forma
    COLOR_YELLOW("Amarilla (particular)", "^[A-Z]{3}-\\d{3}$", 3, "ABC-123"),
    COLOR_YELLOW_MOTORCYCLE("Amarilla (moto)", "^[A-Z]{3}-\\d{2}[A-Z]$", 3, "ABC-12D"),
    COLOR_WHITE("Blanca (público / oficial)", "^[A-Z]{3}-\\d{3}$", 3, "ABC-123"),
    COLOR_BLUE("Azul (diplomático)", "^(CD|CC|OI)-\\d{4}$", 2, "CD-1234"),
    COLOR_GREEN("Verde (remolque)", "^[RS]-\\d{5}$", 1, "R-12345"),
    COLOR_RED("Roja (temporal)", "^[A-Z]-\\d{4}$", 1, "T-1234");

    private final String label;
    private final Pattern pattern;
    //Cantidad de letras antes del guion
    private final int prefixLength;
    private final String example;

    PlateColor(String label, String regex, int prefixLength, String example) {
        this.label = label;
        this.pattern = Pattern.compile(regex);
        this.prefixLength = prefixLength;
        this.example = example;
    }

    //"abc 123", "ABC123" o "abc-123" -> "ABC-123"
    public String normalize(String rawPlate){
        String clean = rawPlate.toUpperCase().replaceAll("[^A-Z0-9]", "");

        if (clean.length() <= prefixLength){
            return clean;
        }

        return clean.substring(0, prefixLength) + "-" + clean.substring(prefixLength);
    }

    public boolean isValid(String normalizedPlate){
        return pattern.matcher(normalizedPlate).matches();
    }

    public String getLabel() {
        return label;
    }

    public String getExample() {
        return example;
    }
}