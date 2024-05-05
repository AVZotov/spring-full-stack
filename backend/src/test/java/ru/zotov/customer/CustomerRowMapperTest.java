package ru.zotov.customer;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerRowMapperTest {

    @Test
    void mapRow() throws SQLException {
        CustomerRowMapper customerRowMapper = new CustomerRowMapper();
        ResultSet resultSet = mock(ResultSet.class);

        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("Random_name");
        when(resultSet.getString("email")).thenReturn("Random_name@gmail.com");
        when(resultSet.getInt("age")).thenReturn(19);

        Customer actual = customerRowMapper.mapRow(resultSet, 1);
        Customer expected = new Customer(1, "Random_name", "Random_name@gmail.com", 19);

        assertThat(actual).isEqualTo(expected);
    }
}