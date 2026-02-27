package com.etecsa.service.dto;

// DTO para respuestas
public class ModbusCommandResponse {

    private String generatorId;
    private String commandType;
    private int address;
    private boolean success;
    private String message;

    public ModbusCommandResponse(String generatorId, String commandType, int address, boolean success, String message) {
        this.generatorId = generatorId;
        this.commandType = commandType;
        this.address = address;
        this.success = success;
        this.message = message;
    }

    // Getters y Setters
    public String getGeneratorId() {
        return generatorId;
    }

    public void setGeneratorId(String generatorId) {
        this.generatorId = generatorId;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
