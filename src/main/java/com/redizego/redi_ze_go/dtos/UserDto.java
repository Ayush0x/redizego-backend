package com.redizego.redi_ze_go.dtos;

import com.redizego.redi_ze_go.entities.enums.Roles;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String name;

    private String email;

//    private String password;

    private Set<Roles> role;
}
