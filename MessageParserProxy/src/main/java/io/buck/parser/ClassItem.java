package io.buck.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by buck on 2017/12/18
 */

public class ClassItem {
    String classFullName;
    String classSimpleName;
    List<MethodItem> methodItems = new ArrayList<>();

    public ClassItem(String classFullName, String classSimpleName) {
        this.classFullName = classFullName;
        this.classSimpleName = classSimpleName;
    }

    public void addMethodItem(MethodItem methodItem) {
        methodItems.add(methodItem);
    }
}
