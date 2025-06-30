package com.grupo5.gamehub.domain.enums;

public enum Role {
    PLAYER,
    ADMIN;
    public String getAuthority(){
        return  "Role_" + this.name();
    }
}