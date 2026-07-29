package domain;
import  java.time.Instant;
public class sample {
    private final long id;
    private final long sampleID;
    private String sampleName;
    private String ownerUsername;

    private final Instant createdAt;
    private Instant updatedAt;
    public sample (long id, long sampleID, String sampleName, String ownerUsername){
        this.id = id;
        this.sampleID = sampleID;
        this.sampleName  = sampleName;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.ownerUsername = ownerUsername;
    }
    public long getSampleID() { return sampleID; }
    public String getSampleName () {return sampleName; }
    public String getOwnerUsername (){return ownerUsername;}
    public void setName(String name) {
        this.sampleName = name;
        this.updatedAt = Instant.now();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof sample)) return false;
        sample smp = (sample) o;
        return id == smp.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}

