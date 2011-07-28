package org.sakaiproject.nakamura.lom.elements;

import org.apache.sling.commons.json.JSONObject;
import org.sakaiproject.nakamura.lom.type.JSONUtil;
import org.sakaiproject.nakamura.lom.type.Serialize;

public class OrComposite extends Serialize {

  private Type type;
  private Name name;
  private String minimumVersion;
  private String maximumVersion;

  public OrComposite() {
    super();
  }
  
  public OrComposite(JSONObject json) {
    super(json);
  }

  @Override
  protected void init() {
    String typeName = "type";
    String nameName = "name";
    String minimumVersionName = "minimumVersion";
    String maxmumVersionName = "maximumVersion";
    JSONObject typeJSON = JSONUtil.getJSONObject(json, typeName);
    if (typeJSON != null) 
      type = new Type(typeJSON);
    
    JSONObject nameJSON = JSONUtil.getJSONObject(json, nameName);
    if (nameJSON != null)
      name = new Name(nameJSON);
    
    minimumVersion = JSONUtil.getStringValue(json, minimumVersionName);
    maximumVersion = JSONUtil.getStringValue(json, maxmumVersionName);
  }
  
  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Name getName() {
    return name;
  }

  public void setName(Name name) {
    this.name = name;
  }

  public String getMinimumVersion() {
    return minimumVersion;
  }

  public void setMinimumVersion(String minimumVersion) {
    this.minimumVersion = minimumVersion;
  }

  public String getMaxmumVersion() {
    return maximumVersion;
  }

  public void setMaximumVersion(String maxmumVersion) {
    this.maximumVersion = maxmumVersion;
  }
  
  @Override
  public String generateXML() {
    StringBuilder sb = new StringBuilder("");
    if (this.getName() != null) 
      sb.append(this.getName().generateXML());
    if (this.getType() != null)
      sb.append(this.getType().generateXML());
    if (this.getMaxmumVersion() != null)
      sb.append("<maximumVersion>" + this.getMaxmumVersion() + "</maximumVersion>");
    if (this.getMinimumVersion() != null)
      sb.append("<minimumVersion>" + this.getMinimumVersion() + "</minimumVersion>");
    if (sb.toString().equals(""))
      return "";
    return new String("<orComposite>" + sb.toString() + "</orComposite>");
  }
}
