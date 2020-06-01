package eu.doppel_helix.github.action.apache_commit_info.icla;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Structure mapping for the ICLA signee list for people not being committers.
 *
 * @see <a href="https://whimsy.apache.org/public/icla-info_noid.json">https://whimsy.apache.org/public/icla-info_noid.json</a>
 */
public class ICLAInfoNoId {
    @JsonProperty(value = "last_updated")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss z")
    private Date lastUpdated;
    @JsonProperty(value = "non_committers")
    private List<String> nonCommitters = new ArrayList<>();

    public ICLAInfoNoId() {
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<String> getNonCommitters() {
        return nonCommitters;
    }

    @Override
    public String toString() {
        return "ICLAInfoNoId{" + "lastUpdated=" + lastUpdated + ", nonCommitters=" + nonCommitters + '}';
    }

}
