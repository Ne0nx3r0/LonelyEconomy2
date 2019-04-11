package com.lonelymc.lonelyeconomy.economy;

public class LonelyEconomyResponse {
    private final LonelyEconomyResponseType response;
    private String message;
    private PlayerAccount account;
    
    public LonelyEconomyResponse(LonelyEconomyResponseType response) {
        this.response = response;
    }
    
    public LonelyEconomyResponse(LonelyEconomyResponseType response,String errorMessage) {
        this.response = response;
        this.message = errorMessage;
    }
    
    public LonelyEconomyResponse(LonelyEconomyResponseType response,PlayerAccount account) {
        this.response = response;
        this.account = account;
    }
    
    public LonelyEconomyResponse(LonelyEconomyResponseType response,PlayerAccount account, String errorMessage) {
        this.response = response;
        this.account = account;
        this.message = errorMessage;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public LonelyEconomyResponseType getType() {
        return this.response;
    }
    
    public boolean wasSuccessful() {
        return this.response == LonelyEconomyResponseType.SUCCESS;
    }

    public PlayerAccount getAccount() {
        return this.account;
    }
}
