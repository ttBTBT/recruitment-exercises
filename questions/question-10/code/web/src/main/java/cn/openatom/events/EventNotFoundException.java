package cn.openatom.events;
public class EventNotFoundException extends BusinessException {
    public EventNotFoundException() { super(404, "活动不存在"); }
}
