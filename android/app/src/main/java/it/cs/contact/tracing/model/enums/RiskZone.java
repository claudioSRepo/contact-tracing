package it.cs.contact.tracing.model.enums;

public enum RiskZone {

    LOW, MEDIUM, HIGH, POSITIVE, NEGATIVE;

    public String toIta() {

        switch (this) {

            case LOW:
                return "BASSA";
            case MEDIUM:
                return "MEDIA";
            case HIGH:
                return "ALTA";
            case POSITIVE:
                return "TAMPONE POSITIVO";
            case NEGATIVE:
                return "TAMPONE NEGATIVO";
            default:
                return this.toString();
        }
    }
}
