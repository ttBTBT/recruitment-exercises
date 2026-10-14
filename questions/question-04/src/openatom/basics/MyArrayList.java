package openatom.basics;
public class MyArrayList<T> {
    private Object[] elements;
    private int size;
    public MyArrayList() { elements = new Object[3]; }
    public void add(T element) {
        if (size == elements.length) {
            Object[] expanded = new Object[elements.length * 2];
            for (int i = 0; i < size; i++) expanded[i] = elements[i];
            System.out.println("触发底层扩容！当前数组容量由 " + elements.length + " 扩容至 " + expanded.length);
            elements = expanded;
        }
        elements[size++] = element;
        System.out.println("成功添加：" + element);
    }
    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (!validIndex(index)) return null;
        return (T) elements[index];
    }
    public void remove(int index) {
        if (!validIndex(index)) return;
        Object removed = elements[index];
        for (int i = index; i < size - 1; i++) elements[i] = elements[i + 1];
        elements[--size] = null;
        System.out.println("删除了索引[" + index + "]的元素(" + removed + ")");
    }
    private boolean validIndex(int index) {
        if (index < 0 || index >= size) { System.out.println("索引越界：" + index + "，当前元素数量：" + size); return false; }
        return true;
    }
    public int size() { return size; }
    public int capacity() { return elements.length; }
}
