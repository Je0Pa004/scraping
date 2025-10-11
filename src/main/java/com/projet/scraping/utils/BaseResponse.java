package com.projet.scraping.utils;

import lombok.*;

import java.util.Objects;

public class BaseResponse <T> {

    private int code;
    private String description;
    private T data;

    // Constructeurs explicites (en plus de Lombok si disponible)
    public BaseResponse() {}

    public BaseResponse(int code, String description, T data) {
        this.code = code;
        this.description = description;
        this.data = data;
    }

    // Getters/Setters explicites (en plus de Lombok si disponible)
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

//code Techinque
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BaseResponse<?> that = (BaseResponse<?>) o;
        return code == that.code && Objects.equals(description, that.description) && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, description, data);
    }

    @Override
    public String toString() {
        return "BaseResponse{" +
                "code=" + code +
                ", description='" + description + '\'' +
                ", data=" + data +
                '}';
    }

}
