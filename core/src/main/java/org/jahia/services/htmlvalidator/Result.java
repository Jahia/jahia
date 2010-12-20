package org.jahia.services.htmlvalidator;

public class Result {
    public enum Type {
        ERROR, WARNING, INFO;
    }
    protected Type type = Type.ERROR;
    protected int line;
    protected int column;
    protected String context;
    protected String message;
    protected String example;
    
    public Result() {
        super();
    }
    
    public Result(String message) {
        super();
        this.message = message;
    }    

    public Result(String message, String context, String example) {
        super();
        this.message = message;
        this.context = context;
        this.example = example;        
    }
    
    public Result(String message, String context, String example, Type type) {
        super();
        this.message = message;
        this.context = context;
        this.example = example;        
        this.type = type;        
    }

    protected String errorType;
    protected Integer level;

    /**
     * Gets the value of the line property.
     * 
     * @return
     *     possible object is int
     *     
     */
    public int getLine() {
        return line;
    }

    /**
     * Sets the value of the line property.
     * 
     * @param value
     *     allowed object is int
     *     
     */
    public void setLine(int value) {
        this.line = value;
    }

    /**
     * Gets the value of the column property.
     * 
     * @return
     *     possible object is int
     *     
     */
    public int getColumn() {
        return column;
    }

    /**
     * Sets the value of the column property.
     * 
     * @param value
     *     allowed object is int
     *     
     */
    public void setColumn(int value) {
        this.column = value;
    }

    /**
     * Gets the value of the context property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContext() {
        return context;
    }

    /**
     * Sets the value of the context property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContext(String value) {
        this.context = value;
    }

    /**
     * Gets the value of the message property.
     * 
     */
    public String getMessage() {
        return this.message;
    }
    
    public void setMessage(String message) {
            this.message = message;
    }

    /**
     * Gets the value of the errortype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorType() {
        return errorType;
    }

    /**
     * Sets the value of the errorType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorType(String value) {
        this.errorType = value;
    }

    /**
     * Gets the value of the level property.
     * 
     * @return
     *     possible object is
     *     {@link int }
     *     
     */
    public Integer getLevel() {
        return level;
    }

    /**
     * Sets the value of the level property.
     * 
     * @param value
     *     allowed object is
     *     {@link int }
     *     
     */
    public void setLevel(Integer value) {
        this.level = value;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

}
