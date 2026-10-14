package openatom.basics;
public final class LogisticsConfig {
    private String systemName = "顺风智能仓储系统";
    private static final LogisticsConfig INSTANCE = new LogisticsConfig();
    private LogisticsConfig() { }
    public static LogisticsConfig getInstance() { return INSTANCE; }
    public String getSystemName() { return systemName; }
}
