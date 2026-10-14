package openatom.basics;
public class StorageBox<T> {
    private T item;
    public void storeItem(T item) { this.item = item; }
    public T retrieveItem() { return item; }
}
