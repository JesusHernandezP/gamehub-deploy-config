package com.grupo5.gamehub.domain.enums;

public enum Result {
    PENDING,
    PLAYER1_WINS,
    PLAYER2_WINS, DRAW;
    public String getAuthority(){
        return  "Result_" + this.name();
    }
}