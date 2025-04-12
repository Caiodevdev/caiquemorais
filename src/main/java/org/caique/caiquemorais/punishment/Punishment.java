package org.caique.caiquemorais.punishment;

public class Punishment {
    private final int id;
    private final String uuid;
    private final String username;
    private final String punisherUuid;
    private final String punisherName;
    private final String reason;
    private final String proofLink;
    private final String punishmentType;
    private final long duration;
    private final long issuedAt;
    private final long expiresAt;
    private final boolean active;
    private final String unbannedBy;

    public Punishment(int id, String uuid, String username, String punisherUuid, String punisherName, String reason,
                      String proofLink, String punishmentType, long duration, long issuedAt, long expiresAt, boolean active, String unbannedBy) {
        this.id = id;
        this.uuid = uuid;
        this.username = username;
        this.punisherUuid = punisherUuid;
        this.punisherName = punisherName;
        this.reason = reason;
        this.proofLink = proofLink;
        this.punishmentType = punishmentType;
        this.duration = duration;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.active = active;
        this.unbannedBy = unbannedBy;
    }

    public int getId() { return id; }
    public String getUuid() { return uuid; }
    public String getUsername() { return username; }
    public String getPunisherUuid() { return punisherUuid; }
    public String getPunisherName() { return punisherName; }
    public String getReason() { return reason; }
    public String getProofLink() { return proofLink; }
    public String getPunishmentType() { return punishmentType; }
    public long getDuration() { return duration; }
    public long getIssuedAt() { return issuedAt; }
    public long getExpiresAt() { return expiresAt; }
    public boolean isActive() { return active; }
    public String getUnbannedBy() { return unbannedBy; }
}