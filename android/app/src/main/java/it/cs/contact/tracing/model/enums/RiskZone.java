package it.cs.contact.tracing.model.enums;

public enum RiskZone {

    LOW, MEDIUM, HIGH, POSITIVE, NEGATIVE;

    public String toIta() {

        switch (this) {

            case LOW:
                return "Rischio\nBasso";
            case MEDIUM:
                return "Rischio\nMedio";
            case HIGH:
                return "Rischio\nAlto";
            case POSITIVE:
                return "Tampone\nPositivo";
            case NEGATIVE:
                return "Tampone\nNegativo";
            default:
                return this.toString();
        }
    }
}
