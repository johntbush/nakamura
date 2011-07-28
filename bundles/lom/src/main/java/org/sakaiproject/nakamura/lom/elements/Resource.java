package org.sakaiproject.nakamura.lom.elements;

import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONObject;
import org.sakaiproject.nakamura.lom.type.JSONUtil;
import org.sakaiproject.nakamura.lom.type.Serialize;

import java.util.ArrayList;
import java.util.List;

public class Resource extends Serialize {

  private List<Identifier> identifier;

  private List<Description> description;
  
  public Resource() {
    super();
  }
  
  public Resource(JSONObject json) {
    super(json);
  }

  @Override
  protected void init() {
    String identifierName = "identifier";
    String descriptionName = "description";
    JSONObject idJSON = JSONUtil.getJSONObject(json, identifierName);
    if (idJSON == null) {
      JSONArray idArray = JSONUtil.getJSONArray(json, identifierName);
      if (idArray != null) {
        for (int i = 0; i < idArray.length(); i++) {
          JSONObject object = idArray.optJSONObject(i);
          if (object != null) {
            addIdentifier(new Identifier(object));
          }
        }
      }
    } else {
      addIdentifier(new Identifier(idJSON));
    }
    
    JSONObject descriptionJSON = JSONUtil.getJSONObject(json, descriptionName);
    if (descriptionJSON == null) {
      JSONArray descriptionArray = JSONUtil.getJSONArray(json, descriptionName);
      if (descriptionArray != null) {
        for (int i = 0; i < descriptionArray.length(); i++) {
          JSONObject object = descriptionArray.optJSONObject(i);
          if (object != null) {
            addDescription(new Description(object));
          }
        }
      }
    } else {
      addDescription(new Description(descriptionJSON));
    }
  }

  public List<Identifier> getIdentifier() {
    return identifier;
  }
  
  public void addIdentifier(Identifier id) {
    if (identifier == null) {
      identifier = new ArrayList<Identifier>();
    }
    identifier.add(id);
  }

  public void setIdentifier(List<Identifier> identifier) {
    this.identifier = identifier;
  }

  public List<Description> getDescription() {
    return description;
  }
  
  public void addDescription(Description d) {
    if (description == null) {
      description = new ArrayList<Description> ();
    }
    description.add(d);
  }

  public void setDescription(List<Description> description) {
    this.description = description;
  }
  
  @Override
  public String generateXML() {
    StringBuilder sb = new StringBuilder("");
    if (this.getDescription() != null) {
      for (int i = 0; i < this.getDescription().size(); i++) 
        sb.append(this.getDescription().get(i).generateXML());
    }
    if (this.getIdentifier() != null) {
      for (int i = 0; i < this.getIdentifier().size(); i++) 
        sb.append(this.getIdentifier().get(i).generateXML());
    }
    if (sb.toString().equals(""))
      return "";
    return new String("<resource>" + sb.toString() + "</resource>");
  }
}