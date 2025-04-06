package org.caique.caiquemorais.punishment;

public class PunishmentType {
    private final String name;
    private final long duration;

    public PunishmentType(String name, long duration) {
        this.name = name;
        this.duration = duration;
    }

    public String getName() { return name; }
    public long getDuration() { return duration; }
}