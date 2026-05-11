package com.vitaltrip.firstaid.constant;

public enum SymptomType {

    BLEEDING("Focus on controlling bleeding via direct pressure, elevation, and pressure points. Include severity assessment and shock prevention."),
    BURNS("Focus on cooling the burn, preventing infection, and determining burn degree. Include removing from heat source and protecting the area."),
    FRACTURE("Focus on immobilization and preventing further injury. Include splinting technique and supporting the injured area."),
    ALLERGIC_REACTION("Focus on identifying and removing the allergen, managing symptoms. Include severe-reaction steps and when to use epinephrine."),
    SEIZURE("Focus on protecting the person during the seizure and recovery position. Include timing the seizure and when to call emergency services."),
    HEATSTROKE("Focus on rapid cooling and moving to shade/cool environment. Include monitoring consciousness and preventing shock."),
    HYPOTHERMIA("Focus on gradual warming and preventing heat loss. Include proper positioning and avoiding rapid rewarming."),
    POISONING("Focus on identifying the poison and appropriate decontamination. Include when NOT to induce vomiting and calling poison control."),
    BREATHING_DIFFICULTY("Focus on positioning for easier breathing and clearing airways. Include recognizing severe respiratory distress."),
    ANIMAL_BITE("Focus on wound cleaning, bleeding control, and infection prevention. Include rabies considerations and when to seek immediate care."),
    FALL_INJURY("Focus on spinal injury precautions and assessing for fractures. Include when to move the person and head injury signs.");

    private final String additionalGuidance;

    SymptomType(String additionalGuidance) {
        this.additionalGuidance = additionalGuidance;
    }

    public String getAdditionalGuidance() {
        return additionalGuidance;
    }
}
