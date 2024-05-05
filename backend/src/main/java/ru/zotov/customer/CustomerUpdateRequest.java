package ru.zotov.customer;

public record CustomerUpdateRequest(
        String name,
        String email,
        Integer age
){}
