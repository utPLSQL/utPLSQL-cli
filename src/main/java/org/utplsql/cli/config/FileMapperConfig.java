package org.utplsql.cli.config;

import java.beans.ConstructorProperties;
import java.util.Map;

public class FileMapperConfig {

    private final String path;
    private final String owner;
    private final String regexExpression;
    private final Map<String, String> typeMapping;
    private final Integer ownerSubexpression;
    private final Integer nameSubexpression;
    private final Integer typeSubexpression;

    @ConstructorProperties({"path", "owner", "regexExpression", "typeMapping", "ownerSubexpression", "nameSubexpression", "typeSubexpression"})
    public FileMapperConfig(String path, String owner, String regexExpression, Map<String, String> typeMapping, Integer ownerSubexpression, Integer nameSubexpression, Integer typeSubexpression) {
        this.path = path;
        this.owner = owner;
        this.regexExpression = regexExpression;
        this.typeMapping = typeMapping;
        this.ownerSubexpression = ownerSubexpression;
        this.nameSubexpression = nameSubexpression;
        this.typeSubexpression = typeSubexpression;
    }

    public String getPath() {
        return path;
    }

    public String getOwner() {
        return owner;
    }

    public String getRegexExpression() {
        return regexExpression;
    }

    public Map<String, String> getTypeMapping() {
        return typeMapping;
    }

    public Integer getOwnerSubexpression() {
        return ownerSubexpression;
    }

    public Integer getNameSubexpression() {
        return nameSubexpression;
    }

    public Integer getTypeSubexpression() {
        return typeSubexpression;
    }
}
