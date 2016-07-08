/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.peter.ivarsson.rest.doc;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 */
public class DataModelInfo {
   
   private List<FieldInfo> fields = new ArrayList<>();

   public List<FieldInfo> getFields() {
      return fields;
   }

   public void setFields( List<FieldInfo> fields ) {
      this.fields = fields;
   }

   
}
