package org.coderthoughts.devicemon.ds;

public @interface MonitorConfig {
    String ctxPrefix() default "/";

    boolean autoRefresh() default false;
    int interval() default 30;
}
