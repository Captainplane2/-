package com.esports.zds.entity;

/**
 * 约战状态枚举
 */
public enum MatchStatus {
    WAITING(0, "招募中"),
    READY(1, "已应战"),
    IN_PROGRESS(2, "正在比赛"),
    FINISHED(3, "比赛结束"),
    EXPIRED(4, "已过期"),
    CANCELLED(5, "已取消");
    
    private final int code;
    private final String desc;
    
    MatchStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public static MatchStatus fromCode(int code) {
        for (MatchStatus status : MatchStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return WAITING;
    }
}