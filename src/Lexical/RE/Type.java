package Lexical.RE;

public enum Type {
    Id("变量名"),
    INTEG("整数"),
    STR("字符串"),
    Keywords("关键字"),
    Operation("操作符"),
    Partion("分隔符"),
    Error("错误"),
    Vn("非终结符"),
    Vt("终结符"),
    UNKNOWN("未知");

    private final String value;

    private Type(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
