package eu.doppel_helix.github.action.apache_commit_info.icla;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Structure mapping for the ICLA signee list for people being committers.
 *
 * @see <a href="https://whimsy.apache.org/public/icla-info.json">https://whimsy.apache.org/public/icla-info.json</a>
 */
public class ICLAInfo {
    @JsonProperty(value = "last_updated")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss z")
    private Date lastUpdated;
    private Map<String,String> committers = new HashMap<>();

    public ICLAInfo() {
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Map<String, String> getCommitters() {
        return committers;
    }

    @Override
    public String toString() {
        return "ICLAInfo{" + "lastUpdated=" + lastUpdated + ", committers=" + committers + '}';
    }
}
