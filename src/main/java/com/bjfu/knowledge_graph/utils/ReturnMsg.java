package com.bjfu.knowledge_graph.utils;

import java.io.Serializable;

// @Data
public class ReturnMsg<T> implements Serializable {

    private String code = SUCCESS;
    private String message;
    private T data;

    // 保留无参和全参构造函数
    public ReturnMsg() {
        this.message = "";
        this.data = null;
    }

    public ReturnMsg(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // --- Getters 和 Setters ---
    // 确保所有字段都有 public 的 getter，Jackson 需要它们来访问字段值
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    // --- 定义返回码 (保持不变) ---
    public static final String SUCCESS="0";
    public static final String FAIL="-1";
    public static final String WAINING="-2";
    public static final String ERROR="-3";

    public static <T> ReturnMsg<T> success(String code, String msg, T data) {
        return new ReturnMsg<>(code, msg, data);
    }

    // 这个方法也要修改，虽然它不处理 data，但为了统一返回类型
    public static <T> ReturnMsg<T> success(String code, String msg) {
        return new ReturnMsg<>(code, msg, null);
    }

    // 你也可以为失败等情况创建泛型工厂方法
    public static <T> ReturnMsg<T> fail(String code, String msg) {
        return new ReturnMsg<>(code, msg, null);
    }
}