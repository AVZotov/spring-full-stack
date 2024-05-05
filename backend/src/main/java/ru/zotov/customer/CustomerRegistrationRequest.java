package ru.zotov.customer;

public record CustomerRegistrationRequest(
        String name,
        String email,
        Integer age
) {
}
