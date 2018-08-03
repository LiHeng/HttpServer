package liheng.io.httpserver.context;

/**
 *
 */
public class ScanConfig {
    private String homePath;
    private String packageName;

    public ScanConfig(String homePath, String packageName) {
        this.homePath = homePath;
        this.packageName = packageName;
    }

    public String getHomePath() {
        return homePath;
    }

    public void setHomePath(String homePath) {
        this.homePath = homePath;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
