package cn.openatom.events;
public class UnauthorizedAccessException extends BusinessException {
    public UnauthorizedAccessException() { super(403, "无权操作其他用户的活动"); }
}
